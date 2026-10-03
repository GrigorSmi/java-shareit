package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;

import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    private ItemRequestMapper() {
    }

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        List<ItemRequestShortDto> itemDtos = items.stream()
                .map(item -> new ItemRequestShortDto(item.getId(), item.getName(), item.getOwner().getId()))
                .collect(Collectors.toList());
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                itemDtos
        );
    }
}