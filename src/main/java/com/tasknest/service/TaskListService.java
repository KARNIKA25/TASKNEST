package com.tasknest.service;

import com.tasknest.dto.TaskListRequest;
import com.tasknest.dto.TaskListResponse;
import com.tasknest.entity.TaskList;
import com.tasknest.entity.User;
import com.tasknest.exception.ResourceNotFoundException;
import com.tasknest.repository.TaskListRepository;
import com.tasknest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskListService {

    private final TaskListRepository taskListRepository;
    private final UserRepository userRepository;

    @Autowired
    public TaskListService(TaskListRepository taskListRepository, UserRepository userRepository) {
        this.taskListRepository = taskListRepository;
        this.userRepository = userRepository;
    }

    public TaskListResponse createTaskList(TaskListRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Task list name cannot be empty");
        }

        TaskList taskList = new TaskList();
        taskList.setName(request.getName().trim());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
            taskList.setUser(user);
        }

        TaskList saved = taskListRepository.save(taskList);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskListResponse> getAllTaskLists() {
        return taskListRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskListResponse getTaskListById(Long id) {
        TaskList taskList = getTaskListEntity(id);
        return mapToResponse(taskList);
    }

    public TaskListResponse updateTaskList(Long id, TaskListRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Task list name cannot be empty");
        }

        TaskList taskList = getTaskListEntity(id);
        taskList.setName(request.getName().trim());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
            taskList.setUser(user);
        }

        TaskList updated = taskListRepository.save(taskList);
        return mapToResponse(updated);
    }

    public void deleteTaskList(Long id) {
        TaskList taskList = getTaskListEntity(id);
        taskListRepository.delete(taskList);
    }

    @Transactional(readOnly = true)
    public TaskList getTaskListEntity(Long id) {
        return taskListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found with id: " + id));
    }

    public TaskListResponse mapToResponse(TaskList taskList) {
        Long userId = taskList.getUser() != null ? taskList.getUser().getId() : null;
        String userName = taskList.getUser() != null ? taskList.getUser().getName() : null;
        int taskCount = taskList.getTasks() != null ? taskList.getTasks().size() : 0;
        return new TaskListResponse(taskList.getId(), taskList.getName(), userId, userName, taskCount);
    }
}
