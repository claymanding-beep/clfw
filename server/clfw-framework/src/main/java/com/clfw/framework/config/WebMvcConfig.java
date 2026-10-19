package com.clfw.framework.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.clfw.common.context.UserContext;
import com.clfw.common.core.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：跨域、Sa-Token 注解鉴权、登录用户上下文注入。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 登录用户在 Sa-Token Session 中的存储 Key */
    public static final String SESSION_LOGIN_USER = "loginUser";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Sa-Token 注解鉴权拦截器
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/auth/captcha", "/doc.html",
                        "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**", "/favicon.ico");

        // 登录用户上下文拦截器
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/**");
    }

    /**
     * 将当前登录用户写入 ThreadLocal，供业务层与自动填充使用。
     */
    static class UserContextInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            if (StpUtil.isLogin()) {
                Object obj = StpUtil.getSession().get(SESSION_LOGIN_USER);
                if (obj instanceof LoginUser loginUser) {
                    UserContext.set(loginUser);
                }
            }
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                    Object handler, Exception ex) {
            UserContext.clear();
        }
    }
}
