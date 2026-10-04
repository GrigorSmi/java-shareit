package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.util.List;

public interface BookingService {

    BookingDto createBooking(Long userId, BookingRequestDto requestDto);

    BookingDto approveBooking(Long userId, Long bookingId, boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getBookingsByBooker(Long userId, State state);

    List<BookingDto> getBookingsByOwner(Long userId, State state);
}