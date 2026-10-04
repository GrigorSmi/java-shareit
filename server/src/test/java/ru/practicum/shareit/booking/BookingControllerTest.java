package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
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

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto booking(BookingStatus status) {
        return new BookingDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                status,
                new BookerDto(2L, "Bob"),
                new ItemDto(10L, "Дрель", "d", true, null));
    }

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.createBooking(eq(2L), any())).thenReturn(booking(BookingStatus.WAITING));

        BookingRequestDto request = new BookingRequestDto(10L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(2));
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingService.approveBooking(1L, 1L, true)).thenReturn(booking(BookingStatus.APPROVED));

        mockMvc.perform(patch("/bookings/1").param("approved", "true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldReturn403WhenApproveByWrongUser() throws Exception {
        when(bookingService.approveBooking(eq(5L), eq(1L), eq(true)))
                .thenThrow(new ForbiddenException("Подтвердить бронирование может только владелец вещи"));

        mockMvc.perform(patch("/bookings/1").param("approved", "true")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingService.getBookingById(2L, 1L)).thenReturn(booking(BookingStatus.WAITING));

        mockMvc.perform(get("/bookings/1").header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn404WhenBookingNotFound() throws Exception {
        when(bookingService.getBookingById(eq(2L), eq(99L))).thenThrow(new NotFoundException("Бронирование не найдено"));

        mockMvc.perform(get("/bookings/99").header("X-Sharer-User-Id", 2L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetBookingsByBookerWithDefaultState() throws Exception {
        when(bookingService.getBookingsByBooker(eq(2L), eq(State.ALL))).thenReturn(List.of(booking(BookingStatus.WAITING)));

        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetBookingsByBookerWithState() throws Exception {
        when(bookingService.getBookingsByBooker(eq(2L), eq(State.PAST))).thenReturn(List.of());

        mockMvc.perform(get("/bookings").param("state", "PAST").header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldGetBookingsByOwner() throws Exception {
        when(bookingService.getBookingsByOwner(eq(1L), eq(State.ALL))).thenReturn(List.of(booking(BookingStatus.APPROVED)));

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}