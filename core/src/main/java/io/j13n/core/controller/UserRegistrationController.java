package io.j13n.core.controller;

import io.j13n.core.model.auth.AuthenticationResponse;
import io.j13n.core.model.auth.UserRegistrationRequest;
import io.j13n.core.service.user.UserRegistrationService;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/security/users/registration")
public class UserRegistrationController {

    private final UserRegistrationService userRegistrationService;

    public UserRegistrationController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<AuthenticationResponse>> registerUser(
            @RequestBody UserRegistrationRequest registrationRequest, ServerHttpRequest request) {

        return userRegistrationService
                .registerUser(registrationRequest, request)
                .thenApply(ResponseEntity::ok);
    }
}
