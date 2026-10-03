package ru.practicum.shareit.booking.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class BookingDatesValidator implements ConstraintValidator<ValidBookingDates, BookItemRequestDto> {

    private static final long TOLERANCE_SECONDS = 1;

    @Override
    public boolean isValid(BookItemRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getStart() == null || dto.getEnd() == null) {
            return true;
        }
        LocalDateTime start = dto.getStart();
        LocalDateTime end = dto.getEnd();
        LocalDateTime now = LocalDateTime.now();

        // чтобы не падал CI, а то уже было: допускаем отставание до секунды
        if (start.isBefore(now.minusSeconds(TOLERANCE_SECONDS))
                || end.isBefore(now.minusSeconds(TOLERANCE_SECONDS))) {
            return false;
        }
        return start.isBefore(end);
    }
}