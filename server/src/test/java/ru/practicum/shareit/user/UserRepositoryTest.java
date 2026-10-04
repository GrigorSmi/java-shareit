package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Test
    void shouldSaveAndAssignId() {
        User saved = repository.save(new User(null, "John", "john@mail.com"));

        assertNotNull(saved.getId());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void shouldFindByEmail() {
        repository.save(new User(null, "John", "john2@mail.com"));

        assertTrue(repository.findByEmail("john2@mail.com").isPresent());
        assertFalse(repository.findByEmail("nobody@mail.com").isPresent());
    }

    @Test
    void shouldDeleteById() {
        User saved = repository.save(new User(null, "John", "john3@mail.com"));

        repository.deleteById(saved.getId());

        assertFalse(repository.findById(saved.getId()).isPresent());
    }
}