package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createUser(String name, String email) throws Exception {
        UserDto user = new UserDto(null, name, email);
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void shouldCreateItem() throws Exception {
        long userId = createUser("Owner", "owner@mail.com");
        ItemDto item = new ItemDto(null, "Дрель", "Мощность 600 вт", true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void shouldReturn404WhenCreateItemWithNonExistentUser() throws Exception {
        ItemDto item = new ItemDto(null, "Дрель", "Мощность 600 вт", true);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenCreateItemWithoutAvailable() throws Exception {
        long userId = createUser("Owner", "owner2@mail.com");
        String body = "{\"name\":\"Дрель\",\"description\":\"Мощность 600 вт\"}";

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUpdateItemByOtherUser() throws Exception {
        long ownerId = createUser("Owner", "owner3@mail.com");
        long otherId = createUser("Other", "other@mail.com");

        ItemDto item = new ItemDto(null, "Дрель", "Мощность 600 вт", true);
        String created = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long itemId = objectMapper.readTree(created).get("id").asLong();

        ItemDto update = new ItemDto(null, "Чужое имя", null, null);
        mockMvc.perform(patch("/items/" + itemId)
                        .header("X-Sharer-User-Id", otherId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateItemByOwner() throws Exception {
        long userId = createUser("Owner", "owner4@mail.com");

        ItemDto item = new ItemDto(null, "Дрель", "Мощность 600 вт", true);
        String created = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long itemId = objectMapper.readTree(created).get("id").asLong();

        ItemDto update = new ItemDto(null, "Новое имя", null, false);
        mockMvc.perform(patch("/items/" + itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldSearchOnlyAvailableItemsCaseInsensitive() throws Exception {
        long userId = createUser("Owner", "owner5@mail.com");

        ItemDto availableItem = new ItemDto(null, "Drill Pro", "Power 600 watt", true);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(availableItem)))
                .andExpect(status().isCreated());

        ItemDto unavailableItem = new ItemDto(null, "Drill Old", "Power 100 watt", false);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unavailableItem)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill Pro"));
    }
}
