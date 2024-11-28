package com.example.ficketevent.global.config.redisson;


import java.lang.reflect.Method;

import com.example.ficketevent.global.common.CustomSpringELParser;
import com.example.ficketevent.global.config.aop.AopForTransaction;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String lockKey = (String) CustomSpringELParser
            .getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());

        RLock rLock = redissonClient.getLock(lockKey);

        if (rLock.isLocked()) {
            throw new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED);
        }

        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                throw new BusinessException(ErrorCode.FAILED_TRY_ROCK);
            }

            return aopForTransaction.proceed(joinPoint);
        } catch (BusinessException e) {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
            throw new BusinessException(ErrorCode.FAILED_DURING_TRANSACTION);
        }
     }
}