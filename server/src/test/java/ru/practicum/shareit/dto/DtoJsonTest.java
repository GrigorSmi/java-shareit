package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
class DtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> bookingRequestJson;

    @Autowired
    private JacksonTester<BookingDto> bookingJson;

    @Autowired
    private JacksonTester<ItemRequestDto> itemRequestJson;

    @Autowired
    private JacksonTester<CommentDto> commentJson;

    @Test
    void shouldDeserializeBookingRequestWithDates() throws Exception {
        BookingRequestDto dto = bookingRequestJson.parseObject(
                "{\"itemId\":1,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-02T10:00:00\"}");

        assertEquals(1L, dto.getItemId());
        assertEquals(LocalDateTime.of(2030, 1, 1, 10, 0), dto.getStart());
        assertEquals(LocalDateTime.of(2030, 1, 2, 10, 0), dto.getEnd());
    }

    @Test
    void shouldSerializeBookingDatesInIsoFormat() throws Exception {
        BookingDto dto = new BookingDto(1L,
                LocalDateTime.of(2030, 1, 1, 10, 0, 0),
                LocalDateTime.of(2030, 1, 2, 10, 0, 0),
                BookingStatus.APPROVED,
                new BookerDto(2L, "Bob"),
                new ItemDto(10L, "Дрель", "d", true, null));

        assertThat(bookingJson.write(dto))
                .extractingJsonPathStringValue("@.start").isEqualTo("2030-01-01T10:00:00");
        assertThat(bookingJson.write(dto))
                .extractingJsonPathStringValue("@.end").isEqualTo("2030-01-02T10:00:00");
    }

    @Test
    void shouldSerializeItemRequestDatesInIsoFormat() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Нужна дрель",
                LocalDateTime.of(2030, 1, 1, 10, 0, 0),
                List.of(new ItemRequestShortDto(10L, "Дрель", 3L)));

        assertThat(itemRequestJson.write(dto))
                .extractingJsonPathStringValue("@.created").isEqualTo("2030-01-01T10:00:00");
    }

    @Test
    void shouldSerializeCommentDateInIsoFormat() throws Exception {
        CommentDto dto = new CommentDto(5L, "Отлично", "Bob",
                LocalDateTime.of(2030, 1, 1, 10, 0, 0));

        assertThat(commentJson.write(dto))
                .extractingJsonPathStringValue("@.created").isEqualTo("2030-01-01T10:00:00");
    }

    @Test
    void shouldParseItemRequestItems() throws Exception {
        ItemRequestDto dto = itemRequestJson.parseObject(
                "{\"id\":5,\"description\":\"desc\",\"created\":\"2030-01-01T10:00:00\","
                        + "\"items\":[{\"id\":1,\"name\":\"Дрель\",\"ownerId\":7}]}");

        assertEquals(1, dto.getItems().size());
        assertEquals("Дрель", dto.getItems().get(0).getName());
        assertEquals(7L, dto.getItems().get(0).getOwnerId());
    }
}