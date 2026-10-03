package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User user(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private ItemDto createItem(Long ownerId) {
        return itemService.createItem(ownerId, new ItemDto(null, "Дрель", "Мощность 600 вт", true, null));
    }

    private Item reload(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow();
    }

    private void finishedBooking(Item item, User booker) {
        bookingRepository.save(new Booking(null,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED));
    }

    @Test
    void shouldCreateItem() {
        User owner = user("Owner", "owner@mail.com");

        ItemDto created = createItem(owner.getId());

        assertNotNull(created.getId());
        assertEquals("Дрель", created.getName());
        assertEquals("Мощность 600 вт", created.getDescription());
        assertTrue(created.getAvailable());
    }

    @Test
    void shouldThrowWhenCreateItemForUnknownUser() {
        assertThrows(NotFoundException.class, () -> createItem(99L));
    }

    @Test
    void shouldCreateItemWithRequest() {
        User owner = user("Owner", "owner@mail.com");
        ItemRequest request = itemRequestRepository.save(
                new ItemRequest(null, "Нужна дрель", owner, LocalDateTime.now()));

        ItemDto created = itemService.createItem(owner.getId(),
                new ItemDto(null, "Дрель", "d", true, request.getId()));

        assertEquals(request.getId(), created.getRequestId());
    }

    @Test
    void shouldThrowWhenCreateItemWithUnknownRequest() {
        User owner = user("Owner", "owner@mail.com");

        assertThrows(NotFoundException.class, () -> itemService.createItem(owner.getId(),
                new ItemDto(null, "Дрель", "d", true, 999L)));
    }

    @Test
    void shouldUpdateItemAndIgnoreBlankFields() {
        User owner = user("Owner", "owner@mail.com");
        ItemDto created = createItem(owner.getId());

        ItemDto updated = itemService.updateItem(owner.getId(), created.getId(),
                new ItemDto(null, "  ", " ", false, null));

        assertEquals("Дрель", updated.getName());
        assertEquals("Мощность 600 вт", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void shouldThrowWhenUpdateItemByOtherUser() {
        User owner = user("Owner", "owner@mail.com");
        User other = user("Other", "other@mail.com");
        ItemDto created = createItem(owner.getId());

        assertThrows(NotFoundException.class, () -> itemService.updateItem(other.getId(), created.getId(),
                new ItemDto(null, "New", null, null, null)));
    }

    @Test
    void shouldReturnItemDetailsWithBookingsAndCommentsForOwner() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        ItemDto created = createItem(owner.getId());
        Item item = reload(created.getId());

        finishedBooking(item, booker);
        bookingRepository.save(new Booking(null, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), item, booker, BookingStatus.APPROVED));
        itemService.addComment(booker.getId(), item.getId(), new CommentDto(null, "Отлично", null, null));

        ItemDetailsDto details = itemService.getItemById(owner.getId(), item.getId());

        assertNotNull(details.getLastBooking());
        assertNotNull(details.getNextBooking());
        assertEquals(1, details.getComments().size());
    }

    @Test
    void shouldHideBookingsFromNonOwner() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        ItemDto created = createItem(owner.getId());
        Item item = reload(created.getId());
        finishedBooking(item, booker);

        ItemDetailsDto details = itemService.getItemById(booker.getId(), item.getId());

        assertNull(details.getLastBooking());
        assertNull(details.getNextBooking());
    }

    @Test
    void shouldThrowWhenGetUnknownItem() {
        User user = user("Owner", "owner@mail.com");

        assertThrows(NotFoundException.class, () -> itemService.getItemById(user.getId(), 999L));
    }

    @Test
    void shouldGetItemsByOwner() {
        User owner = user("Owner", "owner@mail.com");
        createItem(owner.getId());
        createItem(owner.getId());

        List<ItemDetailsDto> items = itemService.getItemsByOwner(owner.getId());

        assertEquals(2, items.size());
    }

    @Test
    void shouldReturnEmptyOwnerItems() {
        User owner = user("Owner", "owner@mail.com");

        assertEquals(0, itemService.getItemsByOwner(owner.getId()).size());
    }

    @Test
    void shouldSearchItems() {
        User owner = user("Owner", "owner@mail.com");
        createItem(owner.getId());

        assertEquals(1, itemService.searchItems("дрель").size());
        assertEquals(0, itemService.searchItems("  ").size());
        assertEquals(0, itemService.searchItems(null).size());
    }

    @Test
    void shouldAddCommentAfterFinishedBooking() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        ItemDto created = createItem(owner.getId());
        Item item = reload(created.getId());
        finishedBooking(item, booker);

        CommentDto comment = itemService.addComment(booker.getId(), item.getId(),
                new CommentDto(null, "Отлично", null, null));

        assertNotNull(comment.getId());
        assertEquals("Отлично", comment.getText());
        assertEquals("Booker", comment.getAuthorName());
        assertEquals(1, commentRepository.findByItem_Id(item.getId()).size());
    }

    @Test
    void shouldThrowWhenCommentWithoutFinishedBooking() {
        User owner = user("Owner", "owner@mail.com");
        User booker = user("Booker", "booker@mail.com");
        ItemDto created = createItem(owner.getId());

        assertThrows(IllegalArgumentException.class, () -> itemService.addComment(booker.getId(),
                created.getId(), new CommentDto(null, "Отлично", null, null)));
    }

    @Test
    void shouldThrowWhenCommentUnknownItem() {
        User user = user("Owner", "owner@mail.com");

        assertThrows(NotFoundException.class, () -> itemService.addComment(user.getId(), 999L,
                new CommentDto(null, "Отлично", null, null)));
    }

    @Test
    void shouldThrowWhenCommentUnknownUser() {
        User owner = user("Owner", "owner@mail.com");
        ItemDto created = createItem(owner.getId());

        assertThrows(NotFoundException.class, () -> itemService.addComment(999L, created.getId(),
                new CommentDto(null, "Отлично", null, null)));
    }
}