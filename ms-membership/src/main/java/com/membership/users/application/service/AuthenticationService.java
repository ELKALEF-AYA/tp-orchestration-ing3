package com.membership.users.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.membership.users.application.dto.LoginRequestDTO;
import com.membership.users.application.dto.LoginResponseDTO;
import com.membership.users.domain.entity.User;
import com.membership.users.domain.repository.UserRepository;
import com.membership.users.infrastructure.exception.ResourceNotFoundException;
import com.membership.users.infrastructure.security.jwt.JwtTokenProvider;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.debug("Tentative de login pour l'email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé: {}", loginRequest.getEmail());
                    return new ResourceNotFoundException("User", "email", loginRequest.getEmail());
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Mot de passe invalide pour: {}", loginRequest.getEmail());
            throw new RuntimeException("Email ou mot de passe invalide");
        }

        if (!user.getActive()) {
            log.warn("Utilisateur désactivé: {}", loginRequest.getEmail());
            throw new RuntimeException("Cet utilisateur est désactivé");
        }

        String token = jwtTokenProvider.generateToken(
                String.valueOf(user.getId()),
                user.getEmail(),
                "USER"
        );

        log.info("Login réussi pour: {}", loginRequest.getEmail());

        return LoginResponseDTO.of(token, 3600000, String.valueOf(user.getId()), user.getEmail());
    }
}