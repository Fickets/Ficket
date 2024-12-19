package com.example.ficketqueue.queue.enums;

import lombok.Getter;

@Getter
public enum QueueStatus {
    WAITING("Waiting"),
    IN_PROGRESS("In Progress"),
    ALMOST_DONE("Almost Done"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String description;

    QueueStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
