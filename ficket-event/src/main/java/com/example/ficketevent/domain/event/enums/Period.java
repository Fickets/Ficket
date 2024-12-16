package com.example.ficketevent.domain.event.enums;

public enum Period {
    DAILY("daily"),
    PREVIOUS_DAILY("previous_daily"),
    WEEKLY("weekly"),
    PREVIOUS_WEEKLY("previous_weekly"),
    MONTHLY("monthly");

    private final String value;

    Period(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
