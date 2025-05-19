package io.j13n.core.config;

import io.j13n.core.model.user.Authority;
import io.j13n.core.model.user.User;
import io.j13n.core.repository.AuthorityRepository;
import io.j13n.core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing data...");
        
        // Create authorities if they don't exist
        Authority userAuthority = createAuthorityIfNotFound("ROLE_USER");
        Authority adminAuthority = createAuthorityIfNotFound("ROLE_ADMIN");
        
        // Create admin user if it doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            log.info("Creating admin user...");
            
            Set<Authority> adminAuthorities = new HashSet<>();
            adminAuthorities.add(userAuthority);
            adminAuthorities.add(adminAuthority);
            
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .email("admin@example.com")
                    .firstName("Admin")
                    .lastName("User")
                    .enabled(true)
                    .authorities(adminAuthorities)
                    .build();
            
            userRepository.save(adminUser);
        }
        
        if (!userRepository.existsByUsername("user")) {
            log.info("Creating regular user...");
            
            Set<Authority> userAuthorities = new HashSet<>();
            userAuthorities.add(userAuthority);
            
            User regularUser = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user"))
                    .email("user@example.com")
                    .firstName("Regular")
                    .lastName("User")
                    .enabled(true)
                    .authorities(userAuthorities)
                    .build();
            
            userRepository.save(regularUser);
        }
        
        log.info("Data initialization completed.");
    }
    
    private Authority createAuthorityIfNotFound(String name) {
        return authorityRepository.findByName(name)
                .orElseGet(() -> {
                    log.info("Creating authority: {}", name);
                    Authority authority = Authority.builder().name(name).build();
                    return authorityRepository.save(authority);
                });
    }
}
