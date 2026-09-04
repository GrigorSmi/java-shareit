package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRepositoryTest {

    private final ItemRepository repository = new ItemRepository();

    @Test
    void shouldSaveAndAssignId() {
        Item item = new Item(null, "Дрель", "Мощность 600 вт", true, 1L);

        Item saved = repository.save(item);

        assertTrue(saved.getId() != null);
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldFindByOwnerId() {
        repository.save(new Item(null, "Дрель", "Мощность 600 вт", true, 1L));
        repository.save(new Item(null, "Отвертка", "Аккумуляторная", true, 1L));
        repository.save(new Item(null, "Перфоратор", "Мощный", true, 2L));

        List<Item> ownerItems = repository.findByOwnerId(1L);

        assertEquals(2, ownerItems.size());
    }

    @Test
    void shouldSearchOnlyAvailableItemsCaseInsensitive() {
        repository.save(new Item(null, "Drill Pro", "Power 600 watt", true, 1L));
        repository.save(new Item(null, "Drill Old", "Power 100 watt", false, 1L));
        repository.save(new Item(null, "Hammer", "Big", true, 1L));

        List<Item> result = repository.search("drill");

        assertEquals(1, result.size());
        assertEquals("Drill Pro", result.get(0).getName());
    }

    @Test
    void shouldSearchInDescription() {
        repository.save(new Item(null, "Инструмент", "Аккумуляторная отвертка", true, 1L));

        List<Item> result = repository.search("аккумулятор");

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmptyForBlankText() {
        repository.save(new Item(null, "Drill", "Power", true, 1L));

        assertTrue(repository.search(null).isEmpty());
        assertTrue(repository.search("   ").isEmpty());
        assertTrue(repository.search("").isEmpty());
    }
}
