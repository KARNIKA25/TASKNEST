package com.tasknest.entity;

public enum Priority {
    LOW,
    MEDIUM,
    HIGH;

    public static Priority fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Priority cannot be empty. Must be LOW, MEDIUM, or HIGH.");
        }
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid priority: '" + value + "'. Priority must be LOW, MEDIUM or HIGH.");
        }
    }
}
