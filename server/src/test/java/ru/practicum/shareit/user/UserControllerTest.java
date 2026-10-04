package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldCreateUser() throws Exception {
        UserDto request = new UserDto(null, "John", "john@mail.com");
        when(userService.createUser(any())).thenReturn(new UserDto(1L, "John", "john@mail.com"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@mail.com"));
    }

    @Test
    void shouldReturn409WhenEmailConflict() throws Exception {
        when(userService.createUser(any())).thenThrow(new ConflictException("Email уже используется"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDto(null, "John", "dup@mail.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        when(userService.updateUser(eq(1L), any())).thenReturn(new UserDto(1L, "New", "new@mail.com"));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDto(null, "New", "new@mail.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldGetUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(new UserDto(1L, "Alice", "alice@mail.com"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@mail.com"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserDto(1L, "Alice", "alice@mail.com"),
                new UserDto(2L, "Bob", "bob@mail.com")));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService).deleteUser(1L);
    }

    @Test
    void shouldReturn404WhenDeleteUnknownUser() throws Exception {
        doThrow(new NotFoundException("Пользователь не найден")).when(userService).deleteUser(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
    }
}