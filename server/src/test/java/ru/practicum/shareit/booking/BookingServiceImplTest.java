package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private Item item(User owner, boolean available) {
        return itemRepository.save(new Item(null, "Дрель", "d", available, owner, null));
    }

    private Booking save(Item item, User booker, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        return bookingRepository.save(new Booking(null, start, end, item, booker, status));
    }

    @Test
    void shouldCreateBooking() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, true);

        BookingDto booking = bookingService.createBooking(booker.getId(),
                new BookingRequestDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertEquals(booker.getId(), booking.getBooker().getId());
    }

    @Test
    void shouldThrowWhenBookOwnItem() {
        User owner = user("Owner", "owner@mail.com");
        Item item = item(owner, true);

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(owner.getId(),
                new BookingRequestDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))));
    }

    @Test
    void shouldThrowWhenItemUnavailable() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, false);

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(booker.getId(),
                new BookingRequestDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))));
    }

    @Test
    void shouldThrowWhenBookerUnknown() {
        User owner = user("Owner", "owner@mail.com");
        Item item = item(owner, true);

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(999L,
                new BookingRequestDto(item.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))));
    }

    @Test
    void shouldThrowWhenItemUnknown() {
        User booker = user("Booker", "booker@mail.com");

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(booker.getId(),
                new BookingRequestDto(999L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))));
    }

    @Test
    void shouldApproveBooking() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, true);
        Booking booking = save(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        BookingDto approved = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void shouldRejectBooking() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, true);
        Booking booking = save(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        BookingDto rejected = bookingService.approveBooking(owner.getId(), booking.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void shouldThrowWhenApproveByWrongUser() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, true);
        Booking booking = save(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        assertThrows(ForbiddenException.class,
                () -> bookingService.approveBooking(booker.getId(), booking.getId(), true));
    }

    @Test
    void shouldThrowWhenApproveNotWaiting() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, true);
        Booking booking = save(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.APPROVED);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.approveBooking(owner.getId(), booking.getId(), true));
    }

    @Test
    void shouldThrowWhenApproveUnknownBooking() {
        User owner = user("Owner", "owner@mail.com");

        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(owner.getId(), 999L, true));
    }

    @Test
    void shouldGetBookingByIdForBookerAndOwner() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, true);
        Booking booking = save(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        assertEquals(booking.getId(), bookingService.getBookingById(booker.getId(), booking.getId()).getId());
        assertEquals(booking.getId(), bookingService.getBookingById(owner.getId(), booking.getId()).getId());
    }

    @Test
    void shouldThrowWhenGetBookingByStranger() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        User stranger = user("Stranger", "stranger@mail.com");
        Item item = item(owner, true);
        Booking booking = save(item, booker, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                BookingStatus.WAITING);

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(stranger.getId(), booking.getId()));
    }

    @Test
    void shouldThrowWhenGetUnknownBooking() {
        User user = user("Owner", "owner@mail.com");

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(user.getId(), 999L));
    }

    @Test
    void shouldGetBookerBookingsByStates() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, true);
        LocalDateTime now = LocalDateTime.now();
        save(item, booker, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);
        save(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        save(item, booker, now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED);
        save(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.REJECTED);

        assertEquals(4, bookingService.getBookingsByBooker(booker.getId(), State.ALL).size());
        assertEquals(1, bookingService.getBookingsByBooker(booker.getId(), State.WAITING).size());
        assertEquals(1, bookingService.getBookingsByBooker(booker.getId(), State.PAST).size());
        assertEquals(2, bookingService.getBookingsByBooker(booker.getId(), State.FUTURE).size());
        assertEquals(1, bookingService.getBookingsByBooker(booker.getId(), State.CURRENT).size());
        assertEquals(1, bookingService.getBookingsByBooker(booker.getId(), State.REJECTED).size());
    }

    @Test
    void shouldThrowWhenGetBookerBookingsUnknownUser() {
        assertThrows(NotFoundException.class, () -> bookingService.getBookingsByBooker(999L, State.ALL));
    }

    @Test
    void shouldGetOwnerBookingsByStates() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, true);
        LocalDateTime now = LocalDateTime.now();
        save(item, booker, now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);
        save(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        save(item, booker, now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED);
        save(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.REJECTED);

        assertEquals(4, bookingService.getBookingsByOwner(owner.getId(), State.ALL).size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), State.WAITING).size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), State.PAST).size());
        assertEquals(2, bookingService.getBookingsByOwner(owner.getId(), State.FUTURE).size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), State.CURRENT).size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), State.REJECTED).size());
    }

    @Test
    void shouldThrowWhenGetOwnerBookingsUnknownUser() {
        assertThrows(NotFoundException.class, () -> bookingService.getBookingsByOwner(999L, State.ALL));
    }

    @Test
    void shouldReturnSortedBookings() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        Item item = item(owner, true);
        LocalDateTime now = LocalDateTime.now();
        save(item, booker, now.plusDays(1), now.plusDays(2), BookingStatus.WAITING);
        save(item, booker, now.plusDays(3), now.plusDays(4), BookingStatus.WAITING);
        save(item, booker, now.plusDays(5), now.plusDays(6), BookingStatus.WAITING);

        List<BookingDto> bookings = bookingService.getBookingsByBooker(booker.getId(), State.ALL);

        assertTrue(bookings.get(0).getStart().isAfter(bookings.get(1).getStart())
                && bookings.get(1).getStart().isAfter(bookings.get(2).getStart()));
    }
}