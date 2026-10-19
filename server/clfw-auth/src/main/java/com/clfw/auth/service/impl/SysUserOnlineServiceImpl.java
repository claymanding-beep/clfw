package com.clfw.auth.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.clfw.auth.domain.SysUserOnline;
import com.clfw.auth.service.SysUserOnlineService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 在线用户服务实现：遍历 Sa-Token 有效 token，从各自 Token-Session 读取在线元数据。
 */
@Service
public class SysUserOnlineServiceImpl implements SysUserOnlineService {

    @Override
    public List<SysUserOnline> list(String ip, String username) {
        // searchTokenValue 返回的是含前缀的完整 Redis 键（如 Authorization:login:token:<token>），
        // 需先裁剪前缀还原为真实 tokenValue，否则 getLoginIdByToken 查不到
        String prefix = StpUtil.stpLogic.splicingKeyTokenValue("");
        List<String> rawKeys = StpUtil.searchTokenValue("", 0, -1, false);
        List<SysUserOnline> result = new ArrayList<>();
        for (String rawKey : rawKeys) {
            String token = rawKey.startsWith(prefix) ? rawKey.substring(prefix.length()) : rawKey;
            if (!StringUtils.hasText(token)) {
                continue;
            }
            // 过滤已失效 token
            if (StpUtil.getLoginIdByToken(token) == null) {
                continue;
            }
            SaSession ts = StpUtil.getTokenSessionByToken(token);
            if (ts == null) {
                continue;
            }
            SysUserOnline online = new SysUserOnline();
            online.setTokenValue(token);
            online.setUserId(toLong(ts.get(TS_USER_ID)));
            online.setUsername(toStr(ts.get(TS_USERNAME)));
            online.setNickname(toStr(ts.get(TS_NICKNAME)));
            online.setIp(toStr(ts.get(TS_IP)));
            online.setBrowser(toStr(ts.get(TS_BROWSER)));
            online.setOs(toStr(ts.get(TS_OS)));
            online.setLoginTime(toLong(ts.get(TS_LOGIN_TIME)));

            if (StringUtils.hasText(ip) && (online.getIp() == null || !online.getIp().contains(ip))) {
                continue;
            }
            if (StringUtils.hasText(username)
                    && (online.getUsername() == null || !online.getUsername().contains(username))) {
                continue;
            }
            result.add(online);
        }
        // 按登录时间倒序（最近登录在前）
        result.sort(Comparator.comparing(SysUserOnline::getLoginTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    @Override
    public void forceLogout(String tokenValue) {
        StpUtil.logoutByTokenValue(tokenValue);
    }

    private String toStr(Object value) {
        return value == null ? null : value.toString();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
