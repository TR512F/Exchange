package com.bubnov.v5.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    @NotNull(message = "Username cannot be empty")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotNull(message = "Email cannot be empty")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotNull(message = "Password cannot be empty")
    @NotBlank(message = "Password cannot be blank")
    private String password;
}