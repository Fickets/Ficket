package com.example.ficketuser.global.utils;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.util.function.Supplier;

public class CircuitBreakerUtils {

    private CircuitBreakerUtils() {
        // 유틸리티 클래스는 인스턴스화 금지
    }

    public static <T> T executeWithCircuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry, 
                                                  String breakerName, 
                                                  Supplier<T> supplier) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(breakerName);
        return circuitBreaker.executeSupplier(supplier);
    }
}
