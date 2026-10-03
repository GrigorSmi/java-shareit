package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user(String name, String email) {
        return userRepository.save(new User(null, name, email));
    }

    private ItemRequest saveRequest(User requestor, String description, LocalDateTime created) {
        return requestRepository.save(new ItemRequest(null, description, requestor, created));
    }

    @Test
    void shouldCreateRequest() {
        User requestor = user("Owner", "owner@mail.com");

        ItemRequestDto created = itemRequestService.createRequest(requestor.getId(),
                new ItemRequestDto(null, "Нужна дрель", null, null));

        assertNotNull(created.getId());
        assertEquals("Нужна дрель", created.getDescription());
        assertNotNull(created.getCreated());
        assertEquals(1, requestRepository.findAll().size());
    }

    @Test
    void shouldThrowWhenCreateRequestUnknownUser() {
        assertThrows(NotFoundException.class, () -> itemRequestService.createRequest(999L,
                new ItemRequestDto(null, "Нужна дрель", null, null)));
    }

    @Test
    void shouldGetRequestsByRequestorSortedWithItems() {
        User requestor = user("Owner", "owner@mail.com");
        ItemRequest older = saveRequest(requestor, "Старый", LocalDateTime.now().minusHours(2));
        ItemRequest newer = saveRequest(requestor, "Новый", LocalDateTime.now());
        itemRepository.save(new Item(null, "Дрель", "d", true, requestor, newer.getId()));

        List<ItemRequestDto> requests = itemRequestService.getRequestsByRequestor(requestor.getId());

        assertEquals(2, requests.size());
        assertEquals(newer.getId(), requests.get(0).getId());
        assertEquals(1, requests.get(0).getItems().size());
        assertEquals("Дрель", requests.get(0).getItems().get(0).getName());
        assertEquals(requestor.getId(), requests.get(0).getItems().get(0).getOwnerId());
        assertEquals(older.getId(), requests.get(1).getId());
    }

    @Test
    void shouldThrowWhenGetRequestsByUnknownUser() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestsByRequestor(999L));
    }

    @Test
    void shouldGetAllRequestsExcludingOwn() {
        User other = user("Other", "other@mail.com");
        User requestor = user("Owner", "owner@mail.com");
        saveRequest(other, "Чужой", LocalDateTime.now());
        saveRequest(requestor, "Свой", LocalDateTime.now());

        List<ItemRequestDto> all = itemRequestService.getAllRequests(requestor.getId());

        assertEquals(1, all.size());
        assertEquals("Чужой", all.get(0).getDescription());
    }

    @Test
    void shouldThrowWhenGetAllRequestsUnknownUser() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getAllRequests(999L));
    }

    @Test
    void shouldGetRequestByIdWithItems() {
        User requestor = user("Owner", "owner@mail.com");
        ItemRequest request = saveRequest(requestor, "Нужна дрель", LocalDateTime.now());
        itemRepository.save(new Item(null, "Дрель", "d", true, requestor, request.getId()));

        ItemRequestDto found = itemRequestService.getRequestById(request.getId());

        assertEquals(request.getId(), found.getId());
        assertEquals("Нужна дрель", found.getDescription());
        assertTrue(found.getItems().size() == 1);
        assertEquals("Дрель", found.getItems().get(0).getName());
    }

    @Test
    void shouldThrowWhenGetUnknownRequestById() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(999L));
    }
}