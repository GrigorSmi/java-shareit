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

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Вещь не найдена");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }
        if (!requestDto.getStart().isBefore(requestDto.getEnd())) {
            throw new IllegalArgumentException("Дата начала должна быть раньше даты окончания");
        }

        Booking booking = new Booking();
        booking.setStart(requestDto.getStart());
        booking.setEnd(requestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
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
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Изменить можно только бронирование в статусе WAITING");
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

        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        return findBookerBookings(userId, state, now, sort).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByOwner(Long userId, State state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        return findOwnerBookings(userId, state, now, sort).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private List<Booking> findBookerBookings(Long userId, State state, LocalDateTime now, Sort sort) {
        switch (state) {
            case CURRENT:
                return bookingRepository.findByBooker_IdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST:
                return bookingRepository.findByBooker_IdAndEndBefore(userId, now, sort);
            case FUTURE:
                return bookingRepository.findByBooker_IdAndStartAfter(userId, now, sort);
            case WAITING:
                return bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED:
                return bookingRepository.findByBooker_IdAndStatus(userId, BookingStatus.REJECTED, sort);
            case ALL:
            default:
                return bookingRepository.findByBooker_Id(userId, sort);
        }
    }

    private List<Booking> findOwnerBookings(Long userId, State state, LocalDateTime now, Sort sort) {
        switch (state) {
            case CURRENT:
                return bookingRepository.findByItem_Owner_IdAndStartBeforeAndEndAfter(userId, now, now, sort);
            case PAST:
                return bookingRepository.findByItem_Owner_IdAndEndBefore(userId, now, sort);
            case FUTURE:
                return bookingRepository.findByItem_Owner_IdAndStartAfter(userId, now, sort);
            case WAITING:
                return bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED:
                return bookingRepository.findByItem_Owner_IdAndStatus(userId, BookingStatus.REJECTED, sort);
            case ALL:
            default:
                return bookingRepository.findByItem_Owner_Id(userId, sort);
        }
    }
}