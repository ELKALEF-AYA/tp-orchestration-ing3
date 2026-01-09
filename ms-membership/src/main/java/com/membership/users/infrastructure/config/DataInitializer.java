package com.membership.users.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.membership.users.domain.entity.User;
import com.membership.users.domain.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        long count = userRepository.count();
        
        if (count == 0) {
            log.info("Initialisation de l'utilisateur de test...");
            
            User user = User.builder()
                    .firstName("Aya")
                    .lastName("Test")
                    .email("aya@test.com")
                    .password(passwordEncoder.encode("password123"))
                    .active(true)
                    .build();
            
            userRepository.save(user);
            log.info("Utilisateur de test créé avec succès");
        }
    }
}