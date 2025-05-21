package io.j13n.core.service.auth;

import io.j13n.core.commons.base.service.CacheService;
import io.j13n.core.commons.security.service.IAuthenticationService;
import io.j13n.core.service.user.UserService;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AuthenticationService implements IAuthenticationService {

    private final UserService userService;

    private final PasswordEncoder pwdEncoder;

    private final CacheService cacheService;

    public AuthenticationService(UserService userService, PasswordEncoder pwdEncoder, CacheService cacheService) {
        this.userService = userService;
        this.pwdEncoder = pwdEncoder;
        this.cacheService = cacheService;
    }

    @Override
    public CompletableFuture<Authentication> getAuthentication(boolean isBasic, String bearerToken, ServerHttpRequest request) {
        return null;
    }
}
