package io.j13n.core.service.auth;

import io.j13n.core.commons.base.service.CacheService;
import io.j13n.core.commons.base.thread.VirtualThreadWrapper;
import io.j13n.core.commons.security.jwt.ContextAuthentication;
import io.j13n.core.commons.security.jwt.ContextUser;
import io.j13n.core.commons.security.jwt.JWTClaims;
import io.j13n.core.commons.security.jwt.JWTUtil;
import io.j13n.core.commons.security.service.IAuthenticationService;
import io.j13n.core.model.auth.AuthenticationRequest;
import io.j13n.core.model.auth.AuthenticationResponse;
import io.j13n.core.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.util.function.Tuple2;

@Service
public class AuthenticationService implements IAuthenticationService {

    private final UserService userService;

    private final CacheService cacheService;

    @Value("${jwt.key:defaultSecretKey}")
    private String tokenKey;

    @Value("${jwt.token.rememberme.expiry:1440}")
    private Integer rememberMeExpiryInMinutes;

    @Value("${jwt.token.default.expiry:60}")
    private Integer defaultExpiryInMinutes;

    public AuthenticationService(UserService userService, CacheService cacheService) {
        this.userService = userService;
        this.cacheService = cacheService;
    }

    public CompletableFuture<AuthenticationResponse> authenticate(
            AuthenticationRequest authRequest, HttpServletRequest request) {
        return VirtualThreadWrapper.flatMap(userService.findByUsername(authRequest.getUserName()), user -> {
            if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

            if (!user.getStatusCode().isInActive())
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account is disabled");

            return VirtualThreadWrapper.flatMap(
                    userService.validatePassword(user, authRequest.getPassword()), isValid -> {
                        if (Boolean.FALSE.equals(isValid))
                            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

                        return VirtualThreadWrapper.flatMap(
                                userService.toContextUser(user),
                                contextUser -> generateToken(contextUser, authRequest.isRememberMe(), request));
                    });
        });
    }

    private CompletableFuture<AuthenticationResponse> generateToken(
            ContextUser user, boolean rememberMe, HttpServletRequest request) {
        int timeInMinutes = rememberMe ? rememberMeExpiryInMinutes : defaultExpiryInMinutes;

        String host = request.getRemoteHost();
        String port = "" + request.getLocalPort();

        Tuple2<String, LocalDateTime> token = JWTUtil.generateToken(JWTUtil.JWTGenerateTokenParameters.builder()
                .userId(user.getId())
                .secretKey(tokenKey)
                .expiryInMin(timeInMinutes)
                .host(host)
                .port(port)
                .build());

        return VirtualThreadWrapper.just(new AuthenticationResponse()
                .setUser(user)
                .setAccessToken(token.getT1())
                .setAccessTokenExpiryAt(token.getT2()));
    }

    @Override
    public CompletableFuture<Authentication> getAuthentication(String bearerToken, HttpServletRequest request) {
        if (bearerToken == null || bearerToken.isBlank())
            return VirtualThreadWrapper.just(new ContextAuthentication(null, false, null, null));

        bearerToken = bearerToken.trim();

        if (bearerToken.startsWith("Bearer ")) bearerToken = bearerToken.substring(7);

        final String token = bearerToken;

        return VirtualThreadWrapper.flatMap(cacheService.get(CACHE_NAME_TOKEN, token), cachedAuth -> {
                    if (cachedAuth != null) return VirtualThreadWrapper.just((Authentication) cachedAuth);

                    return extractAndValidateToken(token, request);
                })
                .exceptionally(e -> new ContextAuthentication(null, false, null, null));
    }

    private CompletableFuture<Authentication> extractAndValidateToken(String token, HttpServletRequest request) {
        try {
            JWTClaims claims = JWTUtil.getClaimsFromToken(tokenKey, token);

            String host = request.getRemoteHost();
            if (!host.equals(claims.getHostName()))
                return VirtualThreadWrapper.just(new ContextAuthentication(null, false, null, null));

            return VirtualThreadWrapper.flatMap(
                    userService.findByUsername(claims.getUserId().toString()), user -> {
                        if (user == null)
                            return VirtualThreadWrapper.just(new ContextAuthentication(null, false, null, null));

                        if (Boolean.FALSE.equals(user.getStatusCode().isInActive()))
                            return VirtualThreadWrapper.just(new ContextAuthentication(null, false, null, null));

                        return VirtualThreadWrapper.flatMap(userService.toContextUser(user), contextUser -> {
                            ContextAuthentication auth = new ContextAuthentication(
                                    contextUser,
                                    true,
                                    token,
                                    LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(defaultExpiryInMinutes));

                            return VirtualThreadWrapper.flatMap(
                                    cacheService.put(CACHE_NAME_TOKEN, auth, token),
                                    ca -> VirtualThreadWrapper.just(auth));
                        });
                    });
        } catch (Exception e) {
            return VirtualThreadWrapper.just(new ContextAuthentication(null, false, null, null));
        }
    }
}
