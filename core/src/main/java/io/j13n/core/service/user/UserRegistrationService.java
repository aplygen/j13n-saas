package io.j13n.core.service.user;

import io.j13n.core.commons.base.thread.VirtualThreadWrapper;
import io.j13n.core.dto.user.User;
import io.j13n.core.enums.UserStatusCode;
import io.j13n.core.model.auth.AuthenticationRequest;
import io.j13n.core.model.auth.AuthenticationResponse;
import io.j13n.core.model.auth.UserRegistrationRequest;
import io.j13n.core.service.auth.AuthenticationService;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service responsible for handling user registration.
 */
@Service
public class UserRegistrationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    public UserRegistrationService(
            UserService userService, PasswordEncoder passwordEncoder, AuthenticationService authenticationService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = authenticationService;
    }

    public CompletableFuture<AuthenticationResponse> registerUser(
            UserRegistrationRequest registrationRequest, ServerHttpRequest request) {
        User user = new User()
                .setUserName(registrationRequest.getUserName())
                .setPassword(registrationRequest.getPassword())
                .setEmailId(registrationRequest.getEmailId())
                .setPhoneNumber(registrationRequest.getPhoneNumber())
                .setFirstName(registrationRequest.getFirstName())
                .setLastName(registrationRequest.getLastName())
                .setMiddleName(registrationRequest.getMiddleName())
                .setLocaleCode(registrationRequest.getLocaleCode());

        return this.createUser(user).thenCompose(registeredUser -> {
            AuthenticationRequest authRequest = new AuthenticationRequest()
                    .setUserName(registeredUser.getUserName())
                    .setPassword(registrationRequest.getPassword())
                    .setRememberMe(false);

            return authenticationService.authenticate(authRequest, request);
        });
    }

    private CompletableFuture<User> createUser(User user) {
        return VirtualThreadWrapper.flatMap(validateNewUser(user), validUser -> {
            validUser.setPassword(passwordEncoder.encode(validUser.getPassword()));
            validUser.setPasswordHashed(true);

            validUser.setStatusCode(UserStatusCode.ACTIVE);

            if (validUser.getAuthorities() == null) validUser.setAuthorities(new HashSet<>());

            validUser.setNoFailedAttempt((short) 0);

            return userService.create(validUser);
        });
    }

    private CompletableFuture<User> validateNewUser(User user) {
        return VirtualThreadWrapper.fromCallable(() -> {
            if (user.getUserName() == null || user.getUserName().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");

            if (user.getPassword() == null || user.getPassword().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");

            User existingUser = userService.findByUsername(user.getUserName()).join();
            if (existingUser != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");

            return user;
        });
    }
}
