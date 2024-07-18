package com.arraywork.photowise.spa;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.arraywork.photowise.controller.HomeController;

/**
 * SPA Aspector
 *
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
    private HomeController controller;

    @Around("@annotation(spaRoute)")
    public Object dispatch(ProceedingJoinPoint joinPoint, SpaRoute spaRoute) throws Throwable {
        Object[] args = joinPoint.getArgs();

        // If spa requested, process the original method
        if (request.getHeader("x-spa-request") != null) {
            return joinPoint.proceed(args);
        }
        // Otherwise, process the layout method
        Model model = (Model) args[0];
        return controller.layout(model);
    }

}