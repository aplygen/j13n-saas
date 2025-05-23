package io.j13n.core.commons.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.Builder;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class JWTUtil {

    private JWTUtil() {}

    public static Tuple2<String, LocalDateTime> generateToken(JWTGenerateTokenParameters params) {

        LocalDateTime expirationTime = LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(params.expiryInMin);

        return Tuples.of(
                Jwts.builder()
                        .setIssuer("qplygen")
                        .setSubject(params.userId.toString())
                        .setClaims(new JWTClaims()
                                .setUserId(params.userId)
                                .setHostName(params.host)
                                .setPort(params.port)
                                .setOneTime(params.oneTime)
                                .getClaimsMap())
                        .setIssuedAt(Date.from(Instant.now()))
                        .setExpiration(Date.from(Instant.now().plus(params.expiryInMin, ChronoUnit.MINUTES)))
                        .signWith(Keys.hmacShaKeyFor(params.secretKey.getBytes()), SignatureAlgorithm.HS512)
                        .compact(),
                expirationTime);
    }

    public static JWTClaims getClaimsFromToken(String secretKey, String token) {

        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build();

        Jws<Claims> parsed = parser.parseClaimsJws(token);

        return JWTClaims.from(parsed);
    }

    @Builder
    public static class JWTGenerateTokenParameters {

        BigInteger userId;
        String secretKey;
        Integer expiryInMin;
        String host;
        String port;

        @Builder.Default
        boolean oneTime = false;
    }
}
