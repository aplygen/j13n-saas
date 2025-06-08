package io.j13n.core.model.auth;

import io.j13n.core.commons.security.jwt.ContextUser;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class AuthenticationResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 8718463183001968129L;

    private ContextUser user;
    private String accessToken;
    private LocalDateTime accessTokenExpiryAt;
}
