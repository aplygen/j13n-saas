package io.j13n.core.service.user;

import io.j13n.commons.thread.VirtualThreadWrapper;
import io.j13n.core.commons.jooq.service.AbstractJOOQUpdatableDataService;
import io.j13n.core.commons.security.jwt.ContextUser;
import io.j13n.core.dao.UserDAO;
import io.j13n.core.dto.user.User;
import io.j13n.core.jooq.core.tables.records.CoreUsersRecord;
import java.util.concurrent.CompletableFuture;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractJOOQUpdatableDataService<CoreUsersRecord, Long, User, UserDAO> {

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder, UserDAO userDAO) {
        this.passwordEncoder = passwordEncoder;
        this.dao = userDAO;
    }

    public CompletableFuture<User> findByUsername(String username) {
        return dao.findByUsername(username);
    }

    public CompletableFuture<Boolean> validatePassword(User user, String password) {

        if (!user.isPasswordHashed()) return VirtualThreadWrapper.just(password.equals(user.getPassword()));

        return VirtualThreadWrapper.just(passwordEncoder.matches(password, user.getPassword()));
    }

    public CompletableFuture<ContextUser> toContextUser(User user) {
        return VirtualThreadWrapper.just(user.toContextUser());
    }

    @Override
    protected CompletableFuture<User> updatableEntity(User entity) {
        return CompletableFuture.supplyAsync(() -> {
            User existingUser = this.read(entity.getId()).join();
            if (existingUser == null) return entity;

            existingUser.setPassword(passwordEncoder.encode(entity.getPassword()));
            existingUser.setFirstName(entity.getFirstName());
            existingUser.setLastName(entity.getLastName());
            existingUser.setMiddleName(entity.getMiddleName());
            existingUser.setLocaleCode(entity.getLocaleCode());
            existingUser.setNoFailedAttempt(entity.getNoFailedAttempt());
            existingUser.setUserName(entity.getUserName());
            existingUser.setEmailId(entity.getEmailId());
            existingUser.setPhoneNumber(entity.getPhoneNumber());
            existingUser.setAuthorities(entity.getAuthorities());

            return existingUser;
        });
    }
}
