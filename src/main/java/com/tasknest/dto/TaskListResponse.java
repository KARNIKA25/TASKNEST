package com.tasknest.dto;

public class TaskListResponse {

    private Long id;
    private String name;
    private Long userId;
    private String userName;
    private int taskCount;

    public TaskListResponse() {
    }

    public TaskListResponse(Long id, String name, Long userId, String userName, int taskCount) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.userName = userName;
        this.taskCount = taskCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }
}
