package com.tasknest.dto;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private int taskListCount;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, int taskListCount) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.taskListCount = taskListCount;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getTaskListCount() {
        return taskListCount;
    }

    public void setTaskListCount(int taskListCount) {
        this.taskListCount = taskListCount;
    }
}
