package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingRequestDto requestDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        LocalDateTime start = requestDto.getStart();
        LocalDateTime end = requestDto.getEnd();
        LocalDateTime now = LocalDateTime.now();

        if (start == null || end == null) {
            throw new IllegalArgumentException("Даты бронирования должны быть заполнены");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Дата начала должна быть раньше даты окончания");
        }
        if (start.isBefore(now)) {
            throw new IllegalArgumentException("Дата начала бронирования не может быть в прошлом");
        }
        if (end.isBefore(now)) {
            throw new IllegalArgumentException("Дата окончания бронирования не может быть в прошлом");
        }

        Booking booking = new Booking(null, start, end, item, booker, BookingStatus.WAITING);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтвердить бронирование может только владелец вещи");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование не найдено");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByBooker(Long userId, State state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return bookingRepository.findByBooker_Id(userId, Sort.by(Sort.Direction.DESC, "start")).stream()
                .filter(booking -> matchesState(booking, state))
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByOwner(Long userId, State state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return bookingRepository.findByItem_Owner_Id(userId, Sort.by(Sort.Direction.DESC, "start")).stream()
                .filter(booking -> matchesState(booking, state))
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private boolean matchesState(Booking booking, State state) {
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case CURRENT:
                return booking.getStart().isBefore(now) && booking.getEnd().isAfter(now);
            case PAST:
                return booking.getEnd().isBefore(now);
            case FUTURE:
                return booking.getStart().isAfter(now);
            case WAITING:
                return booking.getStatus() == BookingStatus.WAITING;
            case REJECTED:
                return booking.getStatus() == BookingStatus.REJECTED;
            case ALL:
            default:
                return true;
        }
    }
}