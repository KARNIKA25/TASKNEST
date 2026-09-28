package com.tasknest.service;

import com.tasknest.dto.UserRequest;
import com.tasknest.dto.UserResponse;
import com.tasknest.entity.User;
import com.tasknest.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUserSuccess() {
        UserRequest request = new UserRequest("Karnika", "karnika@tasknest.com");
        User savedUser = new User("Karnika", "karnika@tasknest.com");
        savedUser.setId(1L);

        when(userRepository.existsByEmail("karnika@tasknest.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("Karnika", response.getName());
        assertEquals("karnika@tasknest.com", response.getEmail());
    }

    @Test
    @DisplayName("Should reject user creation if email already exists")
    void testCreateUserDuplicateEmailThrowsException() {
        UserRequest request = new UserRequest("Karnika", "karnika@tasknest.com");
        when(userRepository.existsByEmail("karnika@tasknest.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should list all users")
    void testGetAllUsers() {
        User user1 = new User("User 1", "user1@example.com");
        user1.setId(1L);

        when(userRepository.findAll()).thenReturn(List.of(user1));

        List<UserResponse> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("User 1", users.get(0).getName());
    }
}
