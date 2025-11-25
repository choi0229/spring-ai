package com.sparta.demo4.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("@annotation(com.sparta.demo4.aop.Loggable)")
    public void loggableMethods() {
        // pointcut marker
    }

    @Before("loggableMethods()")
    public void beforeInvocation(JoinPoint joinPoint) {
        log.info("Starting {} with args {}", joinPoint.getSignature().toShortString(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "loggableMethods()", returning = "result")
    public void afterInvocation(JoinPoint joinPoint, Object result) {
        log.info("Completed {} with result {}", joinPoint.getSignature().toShortString(), result);
    }
}
