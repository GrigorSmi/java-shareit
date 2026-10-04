package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemClient itemClient;

    public ItemController(ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> createItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemDto itemDto) {
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemUpdateDto itemDto) {
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId) {
        return itemClient.get(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentDto commentDto) {
        return itemClient.addComment(userId, itemId, commentDto);
    }
}