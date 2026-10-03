package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void shouldCreateItem() throws Exception {
        ItemDto request = new ItemDto(null, "Дрель", "Мощность 600 вт", true, null);
        when(itemService.createItem(eq(1L), any())).thenReturn(new ItemDto(10L, "Дрель", "Мощность 600 вт", true, null));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldReturn400WhenCreateWithoutUserHeader() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemDto(null, "Дрель", "d", true, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateItem() throws Exception {
        when(itemService.updateItem(eq(1L), eq(10L), any()))
                .thenReturn(new ItemDto(10L, "Новое", "d", false, null));

        mockMvc.perform(patch("/items/10")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemDto(null, "Новое", null, false, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldGetItemById() throws Exception {
        ItemDetailsDto details = new ItemDetailsDto(10L, "Дрель", "d", true, null, null, List.of());
        when(itemService.getItemById(1L, 10L)).thenReturn(details);

        mockMvc.perform(get("/items/10").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.comments").isArray());
    }

    @Test
    void shouldReturn404WhenItemNotFound() throws Exception {
        when(itemService.getItemById(eq(1L), eq(99L))).thenThrow(new NotFoundException("Вещь не найдена"));

        mockMvc.perform(get("/items/99").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldGetItemsByOwner() throws Exception {
        when(itemService.getItemsByOwner(1L)).thenReturn(List.of(
                new ItemDetailsDto(1L, "A", "a", true, null, null, List.of()),
                new ItemDetailsDto(2L, "B", "b", true, null, null, List.of())));

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldSearchItems() throws Exception {
        when(itemService.searchItems("дрель")).thenReturn(List.of(
                new ItemDto(1L, "Дрель", "d", true, null)));

        mockMvc.perform(get("/items/search").param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void shouldAddComment() throws Exception {
        CommentDto response = new CommentDto(5L, "Отлично", "Bob", LocalDateTime.now());
        when(itemService.addComment(eq(2L), eq(10L), any())).thenReturn(response);

        mockMvc.perform(post("/items/10/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Отлично\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отлично"))
                .andExpect(jsonPath("$.authorName").value("Bob"));
    }

    @Test
    void shouldReturn400WhenCommentNotAllowed() throws Exception {
        when(itemService.addComment(eq(2L), eq(10L), any()))
                .thenThrow(new IllegalArgumentException("Оставить отзыв можно только после завершённой аренды вещи"));

        mockMvc.perform(post("/items/10/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Отлично\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}