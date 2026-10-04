package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateUser() {
        UserDto created = userService.createUser(new UserDto(null, "John", "john@mail.com"));

        assertNotNull(created.getId());
        assertEquals("John", created.getName());
        assertTrue(userRepository.findById(created.getId()).isPresent());
    }

    @Test
    void shouldThrowWhenCreateWithDuplicateEmail() {
        userService.createUser(new UserDto(null, "John", "dup@mail.com"));

        assertThrows(ConflictException.class,
                () -> userService.createUser(new UserDto(null, "Jane", "dup@mail.com")));
    }

    @Test
    void shouldUpdateUser() {
        UserDto created = userService.createUser(new UserDto(null, "John", "john@mail.com"));

        UserDto updated = userService.updateUser(created.getId(),
                new UserDto(null, "Johnny", "johnny@mail.com"));

        assertEquals("Johnny", updated.getName());
        assertEquals("johnny@mail.com", updated.getEmail());
    }

    @Test
    void shouldThrowWhenUpdateToExistingEmail() {
        userService.createUser(new UserDto(null, "John", "john@mail.com"));
        UserDto second = userService.createUser(new UserDto(null, "Jane", "jane@mail.com"));

        assertThrows(ConflictException.class,
                () -> userService.updateUser(second.getId(), new UserDto(null, null, "john@mail.com")));
    }

    @Test
    void shouldThrowWhenUpdateUnknownUser() {
        assertThrows(NotFoundException.class,
                () -> userService.updateUser(99L, new UserDto(null, "X", "x@mail.com")));
    }

    @Test
    void shouldGetUserById() {
        UserDto created = userService.createUser(new UserDto(null, "John", "john@mail.com"));

        UserDto found = userService.getUserById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("john@mail.com", found.getEmail());
    }

    @Test
    void shouldThrowWhenGetUnknownUser() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void shouldGetAllUsers() {
        userService.createUser(new UserDto(null, "John", "john@mail.com"));
        userService.createUser(new UserDto(null, "Jane", "jane@mail.com"));

        List<UserDto> all = userService.getAllUsers();

        assertEquals(2, all.size());
    }

    @Test
    void shouldDeleteUser() {
        UserDto created = userService.createUser(new UserDto(null, "John", "john@mail.com"));

        userService.deleteUser(created.getId());

        assertFalse(userRepository.findById(created.getId()).isPresent());
    }

    @Test
    void shouldThrowWhenDeleteUnknownUser() {
        assertThrows(NotFoundException.class, () -> userService.deleteUser(99L));
    }
}