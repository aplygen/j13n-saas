package io.j13n.core.commons.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.j13n.core.commons.security.filter.JWTTokenFilter;
import io.j13n.core.commons.security.service.IAuthenticationService;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

public interface ISecurityConfiguration {

    default SecurityFilterChain springSecurityFilterChain(
            HttpSecurity http, IAuthenticationService authService, ObjectMapper objectMapper, String... exclusionList)
            throws Exception {
        return this.springSecurityFilterChain(http, authService, objectMapper, null, exclusionList);
    }

    default SecurityFilterChain springSecurityFilterChain(
            HttpSecurity http,
            IAuthenticationService authService,
            ObjectMapper objectMapper,
            RequestMatcher matcher,
            String... exclusionList)
            throws Exception {

        JWTTokenFilter jwtFilter = new JWTTokenFilter(authService);

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers("**/internal/**")
                        .permitAll()
                        .requestMatchers("/actuator/**")
                        .permitAll()
                        .requestMatchers(exclusionList)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .headers(headers -> {
                    if (matcher != null)
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                                .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self'"));
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
