package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @Autowired
    private UserRepository userRepository;

    private User createOwner(String email) {
        return userRepository.save(new User(null, "Owner", email));
    }

    @Test
    void shouldSaveAndAssignId() {
        User owner = createOwner("item1@mail.com");
        Item item = new Item(null, "Дрель", "Мощность 600 вт", true, owner, null);

        Item saved = repository.save(item);

        assertNotNull(saved.getId());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldFindByOwnerId() {
        User owner = createOwner("item2@mail.com");
        User other = createOwner("item3@mail.com");
        repository.save(new Item(null, "Дрель", "Мощность 600 вт", true, owner, null));
        repository.save(new Item(null, "Отвертка", "Аккумуляторная", true, owner, null));
        repository.save(new Item(null, "Перфоратор", "Мощный", true, other, null));

        List<Item> ownerItems = repository.findByOwner_Id(owner.getId());

        assertEquals(2, ownerItems.size());
    }

    @Test
    void shouldSearchOnlyAvailableItemsCaseInsensitive() {
        User owner = createOwner("item4@mail.com");
        repository.save(new Item(null, "Дрель ПРО", "Мощность 600 Вт", true, owner, null));
        repository.save(new Item(null, "Дрель б/у", "Мощность 100 Вт", false, owner, null));
        repository.save(new Item(null, "Молоток", "Большой", true, owner, null));

        List<Item> result = repository.search("дрель");

        assertEquals(1, result.size());
        assertEquals("Дрель ПРО", result.get(0).getName());
    }

    @Test
    void shouldSearchInDescription() {
        User owner = createOwner("item5@mail.com");
        repository.save(new Item(null, "Инструмент", "Аккумуляторная отвертка", true, owner, null));

        List<Item> result = repository.search("аккумулятор");

        assertEquals(1, result.size());
    }
}