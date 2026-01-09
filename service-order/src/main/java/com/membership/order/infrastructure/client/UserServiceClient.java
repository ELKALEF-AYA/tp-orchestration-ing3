package com.membership.order.infrastructure.client;

import com.membership.order.application.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.user.url:http://localhost:8080}")
    private String userServiceUrl;

    public boolean isUserActive(Long userId) {
        try {
            log.debug("Vérification de l'utilisateur ID: {} auprès du service User", userId);

            String url = userServiceUrl + "/api/v1/users/" + userId;
            UserDTO user = restTemplate.getForObject(url, UserDTO.class);

            boolean isActive = user != null && Boolean.TRUE.equals(user.getActive());
            log.debug("Utilisateur ID: {} - Actif: {}", userId, isActive);

            return isActive;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Utilisateur ID: {} non trouvé", userId);
            return false;
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de l'utilisateur ID: {}", userId, e);
            throw new RuntimeException("Service User indisponible", e);
        }
    }

    public UserDTO getUserById(Long userId) {
        try {
            log.debug("Récupération des informations de l'utilisateur ID: {}", userId);

            String url = userServiceUrl + "/api/v1/users/" + userId;
            return restTemplate.getForObject(url, UserDTO.class);

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Utilisateur ID: {} non trouvé", userId);
            return null;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'utilisateur ID: {}", userId, e);
            throw new RuntimeException("Service User indisponible", e);
        }
    }

    public boolean isServiceAvailable() {
        try {
            String url = userServiceUrl + "/actuator/health";
            restTemplate.getForObject(url, String.class);
            return true;
        } catch (Exception e) {
            log.warn("Service User indisponible: {}", e.getMessage());
            return false;
        }
    }
}
