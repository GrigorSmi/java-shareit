package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;

public class BookingMapper {

    private BookingMapper() {
    }

    public static BookingDto toDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                new BookerDto(booking.getBooker().getId(), booking.getBooker().getName()),
                ItemMapper.toDto(booking.getItem())
        );
    }
}