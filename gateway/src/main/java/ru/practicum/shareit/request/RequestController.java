package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
public class RequestController {
    private final RequestClient requestClient;

    public RequestController(RequestClient requestClient) {
        this.requestClient = requestClient;
    }

    @PostMapping
    public ResponseEntity<Object> createRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemRequestDto requestDto) {
        return requestClient.create(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.getOwn(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId) {
        return requestClient.getById(userId, requestId);
    }
}