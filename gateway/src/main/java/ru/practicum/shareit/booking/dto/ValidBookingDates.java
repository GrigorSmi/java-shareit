package ru.practicum.shareit.booking.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BookingDatesValidator.class)
public @interface ValidBookingDates {

    String message() default "Некорректные даты бронирования";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}