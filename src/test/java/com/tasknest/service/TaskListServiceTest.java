package com.tasknest.service;

import com.tasknest.dto.TaskListRequest;
import com.tasknest.dto.TaskListResponse;
import com.tasknest.entity.TaskList;
import com.tasknest.entity.User;
import com.tasknest.exception.ResourceNotFoundException;
import com.tasknest.repository.TaskListRepository;
import com.tasknest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskListServiceTest {

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskListService taskListService;

    private User sampleUser;
    private TaskList sampleList;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Alex", "alex@example.com");
        sampleUser.setId(1L);

        sampleList = new TaskList("Academics", sampleUser);
        sampleList.setId(1L);
    }

    @Test
    @DisplayName("Should create task list successfully")
    void testCreateTaskListSuccess() {
        TaskListRequest request = new TaskListRequest("Academics", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(taskListRepository.save(any(TaskList.class))).thenReturn(sampleList);

        TaskListResponse response = taskListService.createTaskList(request);

        assertNotNull(response);
        assertEquals("Academics", response.getName());
        assertEquals(1L, response.getUserId());
    }

    @Test
    @DisplayName("Should throw exception if task list name is blank")
    void testCreateTaskListBlankNameThrowsException() {
        TaskListRequest request = new TaskListRequest("   ", 1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> taskListService.createTaskList(request));
        assertEquals("Task list name cannot be empty", ex.getMessage());
    }

    @Test
    @DisplayName("Should delete task list by ID")
    void testDeleteTaskList() {
        when(taskListRepository.findById(1L)).thenReturn(Optional.of(sampleList));
        doNothing().when(taskListRepository).delete(sampleList);

        assertDoesNotThrow(() -> taskListService.deleteTaskList(1L));
        verify(taskListRepository, times(1)).delete(sampleList);
    }

    @Test
    @DisplayName("Should throw 404 when list not found")
    void testGetTaskListNotFoundThrowsException() {
        when(taskListRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskListService.getTaskListById(99L));
    }
}
