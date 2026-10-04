package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestClient requestClient;

    @Test
    void shouldCreateRequest() throws Exception {
        when(requestClient.create(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"description\":\"Нужна дрель\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenDescriptionBlank() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"description\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(requestClient, never()).create(anyLong(), any());
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(requestClient.getOwn(1L)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        when(requestClient.getAll(1L)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(requestClient.getById(1L, 5L)).thenReturn(ResponseEntity.ok(Map.of("id", 5)));

        mockMvc.perform(get("/requests/5").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}