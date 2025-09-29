package main.java.com.ipm.common.validation.controller;


@RestController
@RequestMapping("/users")
public class UserController {

    @PostMapping
    public String createUser(@Valid @RequestBody CreateUserRequest request) {
        // At this point:
        // 1. All fields validated via annotations
        // 2. All String fields sanitized via RequestBodyAdvice
        return "User created: " + request.getUsername();
    }
}