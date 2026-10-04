package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto request() {
        return new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(),
                List.of(new ItemRequestShortDto(10L, "Дрель", 3L)));
    }

    @Test
    void shouldCreateRequest() throws Exception {
        when(itemRequestService.createRequest(eq(1L), any())).thenReturn(request());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemRequestDto(null, "Нужна дрель", null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void shouldGetOwnRequests() throws Exception {
        when(itemRequestService.getRequestsByRequestor(1L)).thenReturn(List.of(request()));

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].items[0].name").value("Дрель"))
                .andExpect(jsonPath("$[0].items[0].ownerId").value(3));
    }

    @Test
    void shouldGetAllRequests() throws Exception {
        when(itemRequestService.getAllRequests(1L)).thenReturn(List.of(request(), request()));

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetRequestById() throws Exception {
        when(itemRequestService.getRequestById(1L)).thenReturn(request());

        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items[0].id").value(10));
    }

    @Test
    void shouldReturn404WhenRequestNotFound() throws Exception {
        when(itemRequestService.getRequestById(99L)).thenThrow(new NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/99"))
                .andExpect(status().isNotFound());
    }
}