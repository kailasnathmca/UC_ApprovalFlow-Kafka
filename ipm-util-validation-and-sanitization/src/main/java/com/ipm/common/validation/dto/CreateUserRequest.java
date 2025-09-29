package main.java.com.ipm.common.validation.dto;

import jakarta.validation.constraints.*;

public class CreateUserRequest {
    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @Email
    private String email;

    // getters/setters
}