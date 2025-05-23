package io.j13n.core.service.user;

import io.j13n.core.commons.base.thread.VirtualThreadWrapper;
import io.j13n.core.commons.security.jwt.ContextUser;
import io.j13n.core.model.user.Authority;
import io.j13n.core.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CompletableFuture<Optional<io.j13n.core.model.user.User>> findByUsername(String username) {
        return VirtualThreadWrapper.fromCallable(() -> userRepository.findByUsername(username));
    }

    public CompletableFuture<Boolean> validatePassword(io.j13n.core.model.user.User user, String password) {
        return VirtualThreadWrapper.fromCallable(() -> passwordEncoder.matches(password, user.getPassword()));
    }

    public CompletableFuture<ContextUser> toContextUser(io.j13n.core.model.user.User user) {
        return VirtualThreadWrapper.fromCallable(() -> {
            ContextUser contextUser = new ContextUser()
                    .setId(java.math.BigInteger.valueOf(user.getId()))
                    .setUserName(user.getUsername())
                    .setEmailId(user.getEmail())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName())
                    .setPassword(user.getPassword())
                    .setPasswordHashed(true)
                    .setNoFailedAttempt((short) 0);

            List<String> authorities =
                    user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toList());
            contextUser.setStringAuthorities(authorities);

            return contextUser;
        });
    }
}
