package com.airesume.analyzer.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 300) {
                log.warn("[SLOW API QUERY WARN] {} {} took {} ms (Status: {})",
                        request.getMethod(), request.getRequestURI(), duration, response.getStatus());
            } else {
                log.debug("[PERF METRIC] {} {} executed in {} ms",
                        request.getMethod(), request.getRequestURI(), duration);
            }
        }
    }
}
