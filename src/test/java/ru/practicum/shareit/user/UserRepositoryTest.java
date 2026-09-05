package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRepositoryTest {

    private final UserRepository repository = new UserRepository();

    @Test
    void shouldSaveAndAssignId() {
        User user = new User(null, "John", "john@mail.com");

        User saved = repository.save(user);

        assertTrue(saved.getId() != null);
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldFindByEmailCaseInsensitive() {
        repository.save(new User(null, "John", "John@Mail.com"));

        assertTrue(repository.findByEmail("john@mail.com").isPresent());
        assertFalse(repository.findByEmail("nobody@mail.com").isPresent());
    }

    @Test
    void shouldDeleteById() {
        User saved = repository.save(new User(null, "John", "john@mail.com"));

        repository.deleteById(saved.getId());

        assertFalse(repository.findById(saved.getId()).isPresent());
        assertEquals(0, repository.findAll().size());
    }
}
