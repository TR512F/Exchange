package com.bubnov.v5.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignInRequest {
    @NotNull(message = "Username cannot be empty")
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotNull(message = "Password cannot be empty")
    @NotBlank(message = "Password cannot be blank")
    private String password;
}