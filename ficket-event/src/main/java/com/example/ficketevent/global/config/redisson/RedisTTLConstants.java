package com.example.ficketevent.global.config.redisson;

import java.util.concurrent.TimeUnit;

public class RedisTTLConstants {
    public static final long SEAT_LOCK_LEASE_TIME = 480L; // 8분
    public static final TimeUnit SEAT_LOCK_TIME_UNIT = TimeUnit.SECONDS;
}
