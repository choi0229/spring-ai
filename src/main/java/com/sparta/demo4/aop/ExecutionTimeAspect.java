package com.sparta.demo4.aop;

import com.sparta.demo4.config.MonitoringProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExecutionTimeAspect {

    private final MonitoringProperties monitoringProperties;

    @Around("execution(* com.sparta.demo4.service..*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > monitoringProperties.getSlowThresholdMs()) {
                log.warn("Slow execution detected: {} took {} ms", joinPoint.getSignature().toShortString(), elapsed);
            } else {
                log.info("Execution time: {} -> {} ms", joinPoint.getSignature().toShortString(), elapsed);
            }
        }
    }
}
