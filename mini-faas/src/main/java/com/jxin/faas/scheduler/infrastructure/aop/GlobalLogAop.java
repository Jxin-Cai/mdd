package com.jxin.faas.scheduler.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 全局日志切面
 * @author Jxin
 * @version 1.0
 * @since 2020/7/23 15:55
 */
@Order(1)
// @Component
@Aspect
@Slf4j
public class GlobalLogAop {
    /**
     * 在acl层的Pointcut
     */
    @Pointcut("execution(* com.jxin.faas.scheduler.interfaces.acl.*.*.*(..))")
    private void aclPointcut(){}
    /**
     * 在grpc层的Pointcut
     */
    @Pointcut("execution(* com.jxin.faas.scheduler.interfaces.grpc.*.*.*(..))")
    private void grpcPointcut(){}


    @Pointcut("aclPointcut() || grpcPointcut()")
    private void allPointcut(){}

    @Around("allPointcut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        if(log.isDebugEnabled()){
            log.debug("[接口调用],发生调用,funcName: {}#{}", proceedingJoinPoint.getSignature().getDeclaringTypeName(), proceedingJoinPoint.getSignature().getName());
        }
        final Instant start = Instant.now();
        final Object proceed = proceedingJoinPoint.proceed();
        if(log.isDebugEnabled()){
            log.debug("[接口调用],调用结束,funcName: {}#{}, 耗时: {} ms",
                    proceedingJoinPoint.getSignature().getDeclaringTypeName(),
                    proceedingJoinPoint.getSignature().getName(),
                    ChronoUnit.MILLIS.between(start, Instant.now()));
        }
        return proceed;

    }

    @AfterThrowing(throwing="e", pointcut="allPointcut()")
    public void throwing(Throwable e) {
        log.warn("[对外接口],发生异常,errMsg: {}", e.getMessage(), e);
    }
}
