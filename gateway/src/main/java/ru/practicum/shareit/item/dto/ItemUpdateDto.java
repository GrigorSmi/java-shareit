package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.NotBlankIfPresent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemUpdateDto {
    @NotBlankIfPresent
    @Size(max = 255)
    private String name;

    @NotBlankIfPresent
    @Size(max = 512)
    private String description;

    private Boolean available;
}