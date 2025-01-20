package com.syndicated_loan.syndicated_loan.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(public * com.syndicated_loan.syndicated_loan..*Controller.*(..))")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethod(joinPoint, "Controller");
    }

    @Around("execution(public * com.syndicated_loan.syndicated_loan..*Service.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethod(joinPoint, "Service");
    }

    private Object logMethod(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = String.format("%s.%s", className, methodName);

        log.info("[{}] {} - Start", layer, fullMethodName);
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            log.info("[{}] {} - End ({}ms)", layer, fullMethodName, endTime - startTime);
            return result;
        } catch (Throwable e) {
            long endTime = System.currentTimeMillis();
            log.info("[{}] {} - Error ({}ms): {}", layer, fullMethodName, endTime - startTime, e.getMessage());
            throw e;
        }
    }
}
