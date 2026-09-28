package com.tasknest.service;

import com.tasknest.dto.TaskRequest;
import com.tasknest.dto.TaskResponse;
import com.tasknest.entity.Priority;
import com.tasknest.entity.Task;
import com.tasknest.entity.TaskList;
import com.tasknest.exception.ResourceNotFoundException;
import com.tasknest.repository.TaskListRepository;
import com.tasknest.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskListRepository taskListRepository;

    @InjectMocks
    private TaskService taskService;

    private TaskList sampleList;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleList = new TaskList("Academics");
        sampleList.setId(1L);

        sampleTask = new Task("DBMS Project", "Finish TaskNest code", LocalDate.now().plusDays(2), Priority.HIGH, sampleList);
        sampleTask.setId(10L);
    }

    @Test
    @DisplayName("Should successfully create a task with valid inputs")
    void testCreateTaskSuccess() {
        TaskRequest request = new TaskRequest("DBMS Project", "Finish TaskNest code", LocalDate.now().plusDays(2), "HIGH", 1L);

        when(taskListRepository.findById(1L)).thenReturn(Optional.of(sampleList));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("DBMS Project", response.getTitle());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(1L, response.getTaskListId());
        assertFalse(response.isCompleted());
    }

    @Test
    @DisplayName("Rule 3: Should reject task creation if title is empty")
    void testCreateTaskEmptyTitleThrowsException() {
        TaskRequest request = new TaskRequest("", "Description", LocalDate.now(), "LOW", 1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> taskService.createTask(request));
        assertEquals("Task title cannot be empty", ex.getMessage());
    }

    @Test
    @DisplayName("Rule 2: Should reject task creation if priority is invalid")
    void testCreateTaskInvalidPriorityThrowsException() {
        TaskRequest request = new TaskRequest("Test Task", "Description", LocalDate.now(), "SUPER_URGENT", 1L);
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(sampleList));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> taskService.createTask(request));
        assertEquals("Priority must be LOW, MEDIUM or HIGH", ex.getMessage());
    }

    @Test
    @DisplayName("Rule 4 & 5: Should reject task creation if task list does not exist")
    void testCreateTaskNonExistentListThrowsException() {
        TaskRequest request = new TaskRequest("Test Task", "Description", LocalDate.now(), "LOW", 999L);
        when(taskListRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(request));
        assertTrue(ex.getMessage().contains("Task list not found with id: 999"));
    }

    @Test
    @DisplayName("Rule 1: Should filter and return overdue tasks")
    void testGetOverdueTasks() {
        Task overdueTask = new Task("Overdue Assignment", "Overdue desc", LocalDate.now().minusDays(3), Priority.HIGH, sampleList);
        overdueTask.setId(20L);
        overdueTask.setCompleted(false);

        when(taskRepository.findOverdueTasks(any(LocalDate.class))).thenReturn(List.of(overdueTask));

        List<TaskResponse> overdueTasks = taskService.getOverdueTasks();

        assertEquals(1, overdueTasks.size());
        assertEquals("Overdue Assignment", overdueTasks.get(0).getTitle());
        assertTrue(overdueTasks.get(0).isOverdue());
    }

    @Test
    @DisplayName("Should mark task as completed")
    void testMarkAsComplete() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.markAsComplete(10L);

        assertTrue(response.isCompleted());
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    @DisplayName("Should mark task as incomplete")
    void testMarkAsIncomplete() {
        sampleTask.setCompleted(true);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.markAsIncomplete(10L);

        assertFalse(response.isCompleted());
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    @DisplayName("Should successfully move task to another task list")
    void testMoveTask() {
        TaskList targetList = new TaskList("Projects");
        targetList.setId(2L);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(sampleTask));
        when(taskListRepository.findById(2L)).thenReturn(Optional.of(targetList));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.moveTask(10L, 2L);

        assertEquals(2L, response.getTaskListId());
        assertEquals("Projects", response.getTaskListName());
    }
}
