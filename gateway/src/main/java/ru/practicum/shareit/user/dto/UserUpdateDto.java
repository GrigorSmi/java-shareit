package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.NotBlankIfPresent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    @NotBlankIfPresent
    @Size(max = 255)
    private String name;

    @NotBlankIfPresent
    @Email
    @Size(max = 512)
    private String email;
}