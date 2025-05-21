package io.j13n.core.commons.security.service;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;

import java.util.concurrent.CompletableFuture;

public interface IAuthenticationService {

    String CACHE_NAME_TOKEN = "tokenCache";

    CompletableFuture<Authentication> getAuthentication(boolean isBasic, String bearerToken, ServerHttpRequest request);
}
