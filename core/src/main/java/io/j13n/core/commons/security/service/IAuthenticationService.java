package io.j13n.core.commons.security.service;

import java.util.concurrent.CompletableFuture;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;

public interface IAuthenticationService {

    String CACHE_NAME_TOKEN = "tokenCache";

    CompletableFuture<Authentication> getAuthentication(boolean isBasic, String bearerToken, ServerHttpRequest request);
}
