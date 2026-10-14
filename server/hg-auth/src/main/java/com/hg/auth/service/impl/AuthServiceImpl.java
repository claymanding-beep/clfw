package com.hg.auth.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.hg.auth.domain.LoginBody;
import com.hg.auth.domain.UserInfoVo;
import com.hg.auth.service.AuthService;
import com.hg.auth.service.SysUserOnlineService;
import com.hg.common.constant.CacheConstants;
import com.hg.common.constant.CommonConstants;
import com.hg.common.core.LoginUser;
import com.hg.common.enums.ResultCode;
import com.hg.common.exception.BusinessException;
import com.hg.common.utils.PasswordUtils;
import com.hg.framework.config.WebMvcConfig;
import com.hg.system.domain.SysMenu;
import com.hg.system.domain.SysRole;
import com.hg.system.domain.SysUser;
import com.hg.system.service.SysConfigService;
import com.hg.system.service.SysLoginLogService;
import com.hg.system.service.SysMenuService;
import com.hg.system.service.SysRoleService;
import com.hg.system.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现，基于 Sa-Token。
 */
@Service
public class AuthServiceImpl implements AuthService {

    /** 登录验证码开关参数键 */
    private static final String CAPTCHA_ENABLED_KEY = "sys.account.captchaEnabled";
    /** 密码最大重试次数参数键 */
    private static final String PWD_MAX_RETRY_KEY = "sys.pwd.maxRetry";
    /** 账号锁定时长（分钟）参数键 */
    private static final String PWD_LOCK_TIME_KEY = "sys.pwd.lockTime";

    private final SysUserService userService;
    private final SysMenuService menuService;
    private final SysRoleService roleService;
    private final SysLoginLogService loginLogService;
    private final SysConfigService configService;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuthServiceImpl(SysUserService userService, SysMenuService menuService,
                           SysRoleService roleService, SysLoginLogService loginLogService,
                           SysConfigService configService, RedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.menuService = menuService;
        this.roleService = roleService;
        this.loginLogService = loginLogService;
        this.configService = configService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Map<String, Object> login(LoginBody loginBody) {
        String username = loginBody.getUsername();

        // 1. 验证码校验（一次性；验证码失败不计入密码错误次数）
        validateCaptcha(loginBody);

        // 2. 登录失败锁定校验
        int maxRetry = getConfigInt(PWD_MAX_RETRY_KEY, 5);
        int lockTime = getConfigInt(PWD_LOCK_TIME_KEY, 10);
        String lockKey = CacheConstants.PWD_ERR_CNT_KEY + username;
        if (currentRetry(lockKey) >= maxRetry) {
            Long remain = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            long minutes = (remain == null || remain < 0) ? lockTime : remain;
            loginLogService.record(username, getIp(), getBrowser(), getOs(), 0, "账号已锁定");
            throw new BusinessException(ResultCode.USER_ACCOUNT_LOCKED.getCode(),
                    String.format("密码输入错误%d次，账号已锁定，请%d分钟后再试", maxRetry, minutes));
        }

        // 3. 用户存在性与密码校验（失败累加重试计数）
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            int remaining = increaseRetry(lockKey, lockTime, maxRetry);
            loginLogService.record(username, getIp(), getBrowser(), getOs(), 0, "用户不存在");
            throw passwordError(remaining);
        }
        if (!PasswordUtils.matches(loginBody.getPassword(), user.getPassword())) {
            int remaining = increaseRetry(lockKey, lockTime, maxRetry);
            loginLogService.record(username, getIp(), getBrowser(), getOs(), 0, "密码错误");
            throw passwordError(remaining);
        }
        if (user.getStatus() != null && user.getStatus() == CommonConstants.STATUS_DISABLE) {
            loginLogService.record(username, getIp(), getBrowser(), getOs(), 0, "账号已禁用");
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 4. 登录成功：清除失败计数
        redisTemplate.delete(lockKey);

        // 执行登录
        StpUtil.login(user.getId());

        // 构建登录用户上下文并存入 Session
        LoginUser loginUser = buildLoginUser(user);
        StpUtil.getSession().set(WebMvcConfig.SESSION_LOGIN_USER, loginUser);

        // 记录在线元数据到 Token-Session（每个 token 独立，供「在线用户」监控与强退）
        SaSession tokenSession = StpUtil.getTokenSession();
        tokenSession.set(SysUserOnlineService.TS_USER_ID, user.getId());
        tokenSession.set(SysUserOnlineService.TS_USERNAME, user.getUsername());
        tokenSession.set(SysUserOnlineService.TS_NICKNAME, user.getNickname());
        tokenSession.set(SysUserOnlineService.TS_IP, getIp());
        tokenSession.set(SysUserOnlineService.TS_BROWSER, getBrowser());
        tokenSession.set(SysUserOnlineService.TS_OS, getOs());
        tokenSession.set(SysUserOnlineService.TS_LOGIN_TIME, System.currentTimeMillis());

        // 更新最后登录信息
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLoginIp(getIp());
        update.setLoginDate(java.time.LocalDateTime.now());
        userService.updateById(update);

        loginLogService.record(username, getIp(), getBrowser(), getOs(), 1, "登录成功");

        Map<String, Object> result = new HashMap<>();
        result.put("tokenName", StpUtil.getTokenName());
        result.put("tokenValue", StpUtil.getTokenValue());
        return result;
    }

    @Override
    public void logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
    }

    @Override
    public UserInfoVo getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserInfoVo vo = new UserInfoVo();
        vo.setUser(user);
        vo.setPermissions(menuService.selectPermsByUserId(userId));
        if (CommonConstants.SUPER_ADMIN_ID.equals(userId)) {
            vo.setRoles(Set.of(CommonConstants.SUPER_ADMIN_KEY));
        } else {
            List<SysRole> roles = roleService.selectRolesByUserId(userId);
            vo.setRoles(roles.stream().map(SysRole::getRoleKey).collect(Collectors.toSet()));
        }
        return vo;
    }

    @Override
    public List<SysMenu> getRouters() {
        Long userId = StpUtil.getLoginIdAsLong();
        return menuService.selectMenuTreeByUserId(userId);
    }

    @Override
    public SysUser getProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public void updateProfile(SysUser form) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 仅允许修改个人资料字段，用户名/机构/科室/状态/角色/密码均不可通过此接口变更
        SysUser update = new SysUser();
        update.setId(userId);
        update.setNickname(form.getNickname());
        update.setPhone(form.getPhone());
        update.setEmail(form.getEmail());
        update.setSex(form.getSex());
        update.setAvatar(form.getAvatar());
        userService.updateById(update);

        // 同步刷新 Session 中的登录用户昵称，保证顶栏等显示一致
        if (StringUtils.hasText(form.getNickname())) {
            Object obj = StpUtil.getSession().get(WebMvcConfig.SESSION_LOGIN_USER);
            if (obj instanceof LoginUser loginUser) {
                loginUser.setNickname(form.getNickname());
                StpUtil.getSession().set(WebMvcConfig.SESSION_LOGIN_USER, loginUser);
            }
        }
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "新密码不能为空");
        }
        if (newPassword.trim().length() < 6) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "新密码长度不能少于6位");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        if (PasswordUtils.matches(newPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "新密码不能与原密码相同");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(PasswordUtils.encode(newPassword.trim()));
        userService.updateById(update);
    }

    private LoginUser buildLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setOrgId(user.getOrgId());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setPermissions(menuService.selectPermsByUserId(user.getId()));
        if (CommonConstants.SUPER_ADMIN_ID.equals(user.getId())) {
            loginUser.setRoles(Set.of(CommonConstants.SUPER_ADMIN_KEY));
            loginUser.setDataScope(1);
        } else {
            List<SysRole> roles = roleService.selectRolesByUserId(user.getId());
            loginUser.setRoles(roles.stream().map(SysRole::getRoleKey).collect(Collectors.toSet()));
            roles.stream().map(SysRole::getDataScope).filter(java.util.Objects::nonNull)
                    .min(Integer::compareTo).ifPresent(loginUser::setDataScope);
        }
        return loginUser;
    }

    /** 密码错误统一异常：提示剩余可尝试次数 */
    private BusinessException passwordError(int remaining) {
        if (remaining > 0) {
            return new BusinessException(ResultCode.USER_PASSWORD_ERROR.getCode(),
                    String.format("用户名或密码错误，还可尝试%d次", remaining));
        }
        return new BusinessException(ResultCode.USER_PASSWORD_ERROR);
    }

    /** 失败计数 +1 并刷新锁定倒计时，返回剩余可尝试次数（不小于 0） */
    private int increaseRetry(String lockKey, int lockTime, int maxRetry) {
        int count = currentRetry(lockKey) + 1;
        redisTemplate.opsForValue().set(lockKey, String.valueOf(count), lockTime, TimeUnit.MINUTES);
        return Math.max(maxRetry - count, 0);
    }

    /** 读取当前失败次数 */
    private int currentRetry(String lockKey) {
        Object value = redisTemplate.opsForValue().get(lockKey);
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 验证码校验：开关开启时校验 code+uuid，校验后作废（一次性） */
    private void validateCaptcha(LoginBody body) {
        String enabledVal = configService.getConfigValue(CAPTCHA_ENABLED_KEY);
        boolean enabled = enabledVal == null || enabledVal.isBlank() || Boolean.parseBoolean(enabledVal.trim());
        if (!enabled) {
            return;
        }
        String uuid = body.getUuid();
        String code = body.getCode();
        if (!StringUtils.hasText(uuid) || !StringUtils.hasText(code)) {
            loginLogService.record(body.getUsername(), getIp(), getBrowser(), getOs(), 0, "验证码为空");
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
        String key = CacheConstants.CAPTCHA_KEY + uuid;
        Object cached = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        if (cached == null || !code.trim().equalsIgnoreCase(cached.toString())) {
            loginLogService.record(body.getUsername(), getIp(), getBrowser(), getOs(), 0, "验证码错误");
            throw new BusinessException(ResultCode.CAPTCHA_ERROR);
        }
    }

    /** 读取整型参数配置，缺省或非法时返回默认值 */
    private int getConfigInt(String key, int defaultValue) {
        String value = configService.getConfigValue(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String getIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private UserAgent parseAgent() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return UserAgentUtil.parse(request.getHeader("User-Agent"));
    }

    private String getBrowser() {
        UserAgent ua = parseAgent();
        return ua == null || ua.getBrowser() == null ? "unknown" : ua.getBrowser().getName();
    }

    private String getOs() {
        UserAgent ua = parseAgent();
        return ua == null || ua.getOs() == null ? "unknown" : ua.getOs().getName();
    }
}
