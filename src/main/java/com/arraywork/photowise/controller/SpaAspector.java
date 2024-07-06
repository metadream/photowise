package com.arraywork.photowise.controller;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * SPA Aspector
 * @author AiChen
 * @copyright ArrayWork Inc.
 * @since 2024/07/02
 */
@Aspect
@Component
public class SpaAspector {

    @Resource
    private HttpServletRequest request;
    @Resource
    private SpaController controller;

    @Around("@annotation(spaRoute)")
    public Object dispatch(ProceedingJoinPoint joinPoint, SpaRoute spaRoute) throws Throwable {
        // If a partial page requested, process the original method
        Object template = joinPoint.proceed();
        if (request.getHeader("x-http-request") != null) {
            return template;
        }
        // Otherwise, process both layout and original method and return layout
        Object[] args = joinPoint.getArgs();
        Model model = (Model) args[0];
        model.addAttribute("template", template);
        return controller.layout(model);
    }

}