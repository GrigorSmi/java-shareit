package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void shouldGetBookings() throws Exception {
        when(bookingClient.getBookings(eq(1L), eq(BookingState.ALL), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenStateUnknown() throws Exception {
        mockMvc.perform(get("/bookings").param("state", "UNKNOWN").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void shouldReturn400WhenFromNegative() throws Exception {
        mockMvc.perform(get("/bookings").param("from", "-1").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void shouldReturn400WhenSizeZero() throws Exception {
        mockMvc.perform(get("/bookings").param("size", "0").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingClient.bookItem(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 5)));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"itemId\":10,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-02T10:00:00\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenStartMissing() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"itemId\":10,\"end\":\"2030-01-02T10:00:00\"}"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void shouldReturn400WhenStartInPast() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"itemId\":10,\"start\":\"2000-01-01T10:00:00\",\"end\":\"2030-01-02T10:00:00\"}"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void shouldReturn400WhenStartEqualsEnd() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType("application/json")
                        .content("{\"itemId\":10,\"start\":\"2030-01-01T10:00:00\",\"end\":\"2030-01-01T10:00:00\"}"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void shouldGetBooking() throws Exception {
        when(bookingClient.getBooking(1L, 5L)).thenReturn(ResponseEntity.ok(Map.of("id", 5)));

        mockMvc.perform(get("/bookings/5").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetOwnerBookings() throws Exception {
        when(bookingClient.getBookingsByOwner(eq(1L), eq(BookingState.ALL), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldApproveBooking() throws Exception {
        when(bookingClient.approveBooking(1L, 5L, true)).thenReturn(ResponseEntity.ok(Map.of("id", 5)));

        mockMvc.perform(patch("/bookings/5").param("approved", "true").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}