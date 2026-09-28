package com.tasknest.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskListRequest {

    @NotBlank(message = "Task list name cannot be empty")
    private String name;

    private Long userId;

    public TaskListRequest() {
    }

    public TaskListRequest(String name) {
        this.name = name;
    }

    public TaskListRequest(String name, Long userId) {
        this.name = name;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
