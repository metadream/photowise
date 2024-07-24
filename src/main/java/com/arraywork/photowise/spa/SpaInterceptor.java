package com.arraywork.photowise.spa;

import java.io.IOException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.arraywork.photowise.entity.AppSetting;
import com.arraywork.photowise.enums.AccessMode;
import com.arraywork.photowise.service.SettingService;
import com.arraywork.springforce.security.Principal;
import com.arraywork.springforce.security.SecurityContext;

/**
 * Spa Interceptor
 *
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/15
 */
@Component
public class SpaInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    @Resource
    private SecurityContext context;
    @Resource
    private SettingService settingService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws IOException {

        if (handler instanceof HandlerMethod) {
            Principal principal = context.getPrincipal();
            AppSetting setting = settingService.getSetting();

            // Redirect to login page if NOT login and NOT public mode
            if (principal == null && setting.getAccessMode() != AccessMode.PUBLIC) {
                response.sendRedirect("/login");
                return false;
            }
        }
        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).addPathPatterns("/**").excludePathPatterns("/error", "/login");
    }

}