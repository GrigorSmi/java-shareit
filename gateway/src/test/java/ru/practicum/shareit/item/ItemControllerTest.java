package ru.practicum.shareit.item;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void shouldCreateItem() throws Exception {
        when(itemClient.create(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 10)));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"name\":\"Дрель\",\"description\":\"d\",\"available\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenNameBlank() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"description\":\"d\",\"available\":true}"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @Test
    void shouldReturn400WhenAvailableMissing() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"name\":\"Дрель\",\"description\":\"d\"}"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @Test
    void shouldReturn400WhenHeaderMissing() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType("application/json")
                        .content("{\"name\":\"Дрель\",\"description\":\"d\",\"available\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemClient.update(eq(1L), eq(10L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 10)));

        mockMvc.perform(patch("/items/10")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"available\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenUpdateNameTooLong() throws Exception {
        String longName = "a".repeat(256);

        mockMvc.perform(patch("/items/10")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"name\":\"" + longName + "\"}"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).update(anyLong(), anyLong(), any());
    }

    @Test
    void shouldReturn400WhenUpdateNameBlank() throws Exception {
        mockMvc.perform(patch("/items/10")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).update(anyLong(), anyLong(), any());
    }

    @Test
    void shouldGetItem() throws Exception {
        when(itemClient.get(1L, 10L)).thenReturn(ResponseEntity.ok(Map.of("id", 10)));

        mockMvc.perform(get("/items/10").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetItemsByOwner() throws Exception {
        when(itemClient.getByOwner(1L)).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemClient.search("дрель")).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddComment() throws Exception {
        when(itemClient.addComment(eq(2L), eq(10L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 5)));

        mockMvc.perform(post("/items/10/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType("application/json")
                        .content("{\"text\":\"Отлично\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenCommentBlank() throws Exception {
        mockMvc.perform(post("/items/10/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType("application/json")
                        .content("{\"text\":\"\"}"))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }
}