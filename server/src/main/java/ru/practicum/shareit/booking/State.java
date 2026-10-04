package ru.practicum.shareit.booking;

/**
 * Возможные значения параметра state при поиске бронирований.
 */
public enum State {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED
}