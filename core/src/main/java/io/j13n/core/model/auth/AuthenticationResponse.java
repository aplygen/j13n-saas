package io.j13n.core.model.auth;

import io.j13n.core.commons.security.jwt.ContextUser;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AuthenticationResponse {
    private ContextUser user;
    private String accessToken;
    private LocalDateTime accessTokenExpiryAt;
}
