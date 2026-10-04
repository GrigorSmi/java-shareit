package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ValidBookingDates
public class BookItemRequestDto {
    @NotNull
    private Long itemId;
    @NotNull
    private LocalDateTime start;
    @NotNull
    private LocalDateTime end;
}
