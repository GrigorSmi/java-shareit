package ru.practicum.shareit.request;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ResponseEntity<ItemRequestDto> createRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody ItemRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemRequestService.createRequest(userId, requestDto));
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getRequestsByRequestor(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemRequestService.getRequestsByRequestor(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemRequestService.getAllRequests(userId));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getRequestById(@PathVariable Long requestId) {
        return ResponseEntity.ok(itemRequestService.getRequestById(requestId));
    }
}