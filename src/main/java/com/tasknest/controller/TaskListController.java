package com.tasknest.controller;

import com.tasknest.dto.TaskListRequest;
import com.tasknest.dto.TaskListResponse;
import com.tasknest.service.TaskListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*")
public class TaskListController {

    private final TaskListService taskListService;

    @Autowired
    public TaskListController(TaskListService taskListService) {
        this.taskListService = taskListService;
    }

    @PostMapping
    public ResponseEntity<TaskListResponse> createTaskList(@Valid @RequestBody TaskListRequest request) {
        TaskListResponse response = taskListService.createTaskList(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskListResponse>> getAllTaskLists() {
        List<TaskListResponse> lists = taskListService.getAllTaskLists();
        return ResponseEntity.ok(lists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskListResponse> getTaskListById(@PathVariable Long id) {
        TaskListResponse response = taskListService.getTaskListById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskListResponse> updateTaskList(@PathVariable Long id, @Valid @RequestBody TaskListRequest request) {
        TaskListResponse response = taskListService.updateTaskList(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskList(@PathVariable Long id) {
        taskListService.deleteTaskList(id);
        return ResponseEntity.noContent().build();
    }
}
