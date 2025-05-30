package io.j13n.core.commons.security.filter;

import io.j13n.core.commons.security.service.IAuthenticationService;
import io.j13n.core.commons.security.util.ServerHttpRequestUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JWTTokenFilter extends OncePerRequestFilter {

    private final IAuthenticationService authService;

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        String bearerToken = ServerHttpRequestUtil.extractBearerToken(request);

        if (bearerToken == null || bearerToken.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        authService
                .getAuthentication(bearerToken, request)
                .thenAccept(authentication -> {
                    if (authentication != null && authentication.isAuthenticated())
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                })
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        logger.error("Error authenticating with token", throwable);
                        SecurityContextHolder.clearContext();
                    }
                    try {
                        filterChain.doFilter(request, response);
                    } catch (IOException | ServletException e) {
                        logger.error("Error proceeding filter chain", e);
                    }
                });
    }
}
