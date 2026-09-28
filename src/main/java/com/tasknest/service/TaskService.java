package com.tasknest.service;

import com.tasknest.dto.TaskRequest;
import com.tasknest.dto.TaskResponse;
import com.tasknest.entity.Priority;
import com.tasknest.entity.Task;
import com.tasknest.entity.TaskList;
import com.tasknest.exception.ResourceNotFoundException;
import com.tasknest.repository.TaskListRepository;
import com.tasknest.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskListRepository taskListRepository) {
        this.taskRepository = taskRepository;
        this.taskListRepository = taskListRepository;
    }

    public TaskResponse createTask(TaskRequest request) {
        // Business Rule 3: Task title cannot be empty
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        // Business Rule 4 & 5: A task must belong to an existing task list
        if (request.getTaskListId() == null) {
            throw new IllegalArgumentException("Task must belong to a task list (taskListId cannot be null)");
        }
        TaskList taskList = taskListRepository.findById(request.getTaskListId())
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found with id: " + request.getTaskListId()));

        // Business Rule 2: Priority validation
        Priority priority = parsePriority(request.getPriority());

        Task task = new Task();
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(priority);
        task.setTaskList(taskList);
        task.setCompleted(request.getCompleted() != null && request.getCompleted());

        Task saved = taskRepository.save(task);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = getTaskEntity(id);
        return mapToResponse(task);
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = getTaskEntity(id);

        // Business Rule 3: Task title cannot be empty
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }

        // Business Rule 2: Priority validation
        Priority priority = parsePriority(request.getPriority());

        // Business Rule 4 & 5: Task list must exist
        if (request.getTaskListId() != null) {
            TaskList taskList = taskListRepository.findById(request.getTaskListId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task list not found with id: " + request.getTaskListId()));
            task.setTaskList(taskList);
        }

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(priority);
        if (request.getCompleted() != null) {
            task.setCompleted(request.getCompleted());
        }

        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    public void deleteTask(Long id) {
        Task task = getTaskEntity(id);
        taskRepository.delete(task);
    }

    public TaskResponse markAsComplete(Long id) {
        Task task = getTaskEntity(id);
        task.setCompleted(true);
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    public TaskResponse markAsIncomplete(Long id) {
        Task task = getTaskEntity(id);
        task.setCompleted(false);
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTodayTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findByDueDate(today).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks() {
        // Business Rule 1: An overdue + incomplete task must appear in overdue view until completed.
        // Once completed = true, it must NOT appear.
        LocalDate today = LocalDate.now();
        return taskRepository.findOverdueTasks(today).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getCompletedTasks() {
        return taskRepository.findByCompletedTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse moveTask(Long taskId, Long targetListId) {
        Task task = getTaskEntity(taskId);
        TaskList targetList = taskListRepository.findById(targetListId)
                .orElseThrow(() -> new ResourceNotFoundException("Target task list not found with id: " + targetListId));

        task.setTaskList(targetList);
        Task updated = taskRepository.save(task);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByListId(Long listId) {
        // Verify list exists
        if (!taskListRepository.existsById(listId)) {
            throw new ResourceNotFoundException("Task list not found with id: " + listId);
        }
        return taskRepository.findByTaskListId(listId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Task getTaskEntity(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    private Priority parsePriority(String priorityStr) {
        if (priorityStr == null || priorityStr.trim().isEmpty()) {
            return Priority.MEDIUM; // Default priority if not specified
        }
        try {
            return Priority.valueOf(priorityStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Priority must be LOW, MEDIUM or HIGH");
        }
    }

    private TaskResponse mapToResponse(Task task) {
        LocalDate today = LocalDate.now();
        boolean isOverdue = task.getDueDate() != null && task.getDueDate().isBefore(today) && !task.isCompleted();
        Long listId = task.getTaskList() != null ? task.getTaskList().getId() : null;
        String listName = task.getTaskList() != null ? task.getTaskList().getName() : null;

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getPriority(),
                task.isCompleted(),
                listId,
                listName,
                isOverdue
        );
    }
}
