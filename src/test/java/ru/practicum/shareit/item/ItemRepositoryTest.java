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
        Item item = new Item(null, "Дрель", "Мощность 600 вт", true, 1L, null);

        Item saved = repository.save(item);

        assertTrue(saved.getId() != null);
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldFindByOwnerId() {
        repository.save(new Item(null, "Дрель", "Мощность 600 вт", true, 1L, null));
        repository.save(new Item(null, "Отвертка", "Аккумуляторная", true, 1L, null));
        repository.save(new Item(null, "Перфоратор", "Мощный", true, 2L, null));

        List<Item> ownerItems = repository.findByOwnerId(1L);

        assertEquals(2, ownerItems.size());
    }

    @Test
    void shouldSearchOnlyAvailableItemsCaseInsensitive() {
        repository.save(new Item(null, "Дрель ПРО", "Мощность 600 Вт", true, 1L, null));
        repository.save(new Item(null, "Дрель б/у", "Мощность 100 Вт", false, 1L, null));
        repository.save(new Item(null, "Молоток", "Большой", true, 1L, null));

        List<Item> result = repository.search("дрель");

        assertEquals(1, result.size());
        assertEquals("Дрель ПРО", result.get(0).getName());
    }

    @Test
    void shouldSearchInDescription() {
        repository.save(new Item(null, "Инструмент", "Аккумуляторная отвертка", true, 1L, null));

        List<Item> result = repository.search("аккумулятор");

        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmptyForBlankText() {
        repository.save(new Item(null, "Дрель", "Мощность", true, 1L, null));

        assertTrue(repository.search(null).isEmpty());
        assertTrue(repository.search("   ").isEmpty());
        assertTrue(repository.search("").isEmpty());
    }
}

