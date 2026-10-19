package com.clfw.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clfw.common.constant.CommonConstants;
import com.clfw.common.core.PageResult;
import com.clfw.common.enums.ResultCode;
import com.clfw.common.exception.BusinessException;
import com.clfw.common.utils.AesUtils;
import com.clfw.common.utils.PasswordUtils;
import com.clfw.system.domain.SysUser;
import com.clfw.system.domain.SysUserRole;
import com.clfw.system.domain.dto.SysUserQuery;
import com.clfw.system.mapper.SysUserMapper;
import com.clfw.system.mapper.SysUserRoleMapper;
import com.clfw.system.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现。
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;

    public SysUserServiceImpl(SysUserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public PageResult<SysUser> pageUsers(SysUserQuery query) {
        // 手机号加密存储，仅支持精确匹配（先加密查询值再与密文比对）
        String phoneCipher = StringUtils.hasText(query.getPhone())
                ? AesUtils.encrypt(query.getPhone().trim()) : null;
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.hasText(query.getNickname()), SysUser::getNickname, query.getNickname())
                .eq(phoneCipher != null, SysUser::getPhone, phoneCipher)
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(query.getOrgId() != null, SysUser::getOrgId, query.getOrgId())
                .orderByDesc(SysUser::getCreateTime);

        if (query.getRoleId() != null) {
            List<Long> userIds = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getRoleId, query.getRoleId()))
                    .stream().map(SysUserRole::getUserId).collect(Collectors.toList());
            if (userIds.isEmpty()) {
                return PageResult.of(0, List.of());
            }
            wrapper.in(SysUser::getId, userIds);
        }

        Page<SysUser> page = page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    @Override
    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(SysUser user) {
        // 用户名唯一校验
        if (getByUsername(user.getUsername()) != null) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        String raw = StringUtils.hasText(user.getPassword()) ? user.getPassword() : CommonConstants.DEFAULT_PASSWORD;
        user.setPassword(PasswordUtils.encode(raw));
        if (user.getStatus() == null) {
            user.setStatus(CommonConstants.STATUS_ENABLE);
        }
        save(user);
        assignRoles(user.getId(), user.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        // 密码不通过此接口修改
        user.setPassword(null);
        updateById(user);
        if (user.getRoleIds() != null) {
            assignRoles(user.getId(), user.getRoleIds());
        }
    }

    @Override
    public void resetPassword(Long userId, String rawPassword) {
        SysUser user = new SysUser();
        user.setId(userId);
        String raw = StringUtils.hasText(rawPassword) ? rawPassword : CommonConstants.DEFAULT_PASSWORD;
        user.setPassword(PasswordUtils.encode(raw));
        updateById(user);
    }

    @Override
    public List<Long> selectRoleIdsByUserId(Long userId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                userRoleMapper.insert(new SysUserRole(userId, roleId));
            }
        }
    }
}
