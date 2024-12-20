package com.example.ficketqueue.queue.enums;

import lombok.Getter;

@Getter
public enum WorkStatus {
    ORDER_RIGHT_LOST("User lost the right to place an order"),
    SEAT_RESERVATION_RELEASED("Seat reservation has been released");

    private final String description;

    WorkStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
