package com.tasknest.controller;

import com.tasknest.dto.TaskRequest;
import com.tasknest.dto.TaskResponse;
import com.tasknest.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> markAsComplete(@PathVariable Long id) {
        TaskResponse response = taskService.markAsComplete(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/incomplete")
    public ResponseEntity<TaskResponse> markAsIncomplete(@PathVariable Long id) {
        TaskResponse response = taskService.markAsIncomplete(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    public ResponseEntity<List<TaskResponse>> getTodayTasks() {
        List<TaskResponse> tasks = taskService.getTodayTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> getOverdueTasks() {
        List<TaskResponse> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<TaskResponse>> getCompletedTasks() {
        List<TaskResponse> tasks = taskService.getCompletedTasks();
        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{taskId}/move/{listId}")
    public ResponseEntity<TaskResponse> moveTask(@PathVariable Long taskId, @PathVariable Long listId) {
        TaskResponse response = taskService.moveTask(taskId, listId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list/{listId}")
    public ResponseEntity<List<TaskResponse>> getTasksByListId(@PathVariable Long listId) {
        List<TaskResponse> tasks = taskService.getTasksByListId(listId);
        return ResponseEntity.ok(tasks);
    }
}
