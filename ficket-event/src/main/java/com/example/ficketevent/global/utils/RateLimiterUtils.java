package com.example.ficketevent.global.utils;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

import java.util.function.Supplier;

public class RateLimiterUtils {

    private RateLimiterUtils() {
        // 유틸리티 클래스는 인스턴스화 금지
    }

    public static <T> T executeWithRateLimiter(RateLimiterRegistry rateLimiterRegistry,
                                               String limiterName,
                                               Supplier<T> supplier) {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(limiterName);
        return RateLimiter.decorateSupplier(rateLimiter, supplier).get();
    }
}
