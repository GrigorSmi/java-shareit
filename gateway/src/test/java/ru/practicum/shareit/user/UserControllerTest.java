package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

    @MockBean
    private UserClient userClient;

    @Test
    void shouldCreateUser() throws Exception {
        when(userClient.create(any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content("{\"name\":\"John\",\"email\":\"john@mail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn400WhenEmailInvalid() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content("{\"name\":\"John\",\"email\":\"user.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(userClient, never()).create(any());
    }

    @Test
    void shouldReturn400WhenNameBlank() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"email\":\"john@mail.com\"}"))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).create(any());
    }

    @Test
    void shouldReturn400WhenUpdateEmailInvalid() throws Exception {
        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content("{\"email\":\"user.com\"}"))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).update(anyLong(), any());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        when(userClient.update(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content("{\"name\":\"New\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenUpdateNameBlank() throws Exception {
        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).update(anyLong(), any());
    }

    @Test
    void shouldReturn400WhenUpdateEmailBlank() throws Exception {
        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(userClient, never()).update(anyLong(), any());
    }

    @Test
    void shouldGetUser() throws Exception {
        when(userClient.get(1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        when(userClient.getAll()).thenReturn(ResponseEntity.ok(java.util.List.of()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        when(userClient.delete(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient).delete(1L);
    }
}