package com.membership.order.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration du RestTemplate avec support de la méthode HTTP PATCH
 * via Apache HttpClient
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Utiliser Apache HttpClient qui supporte nativement PATCH
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        // Configuration des timeouts (en millisecondes)
        requestFactory.setConnectTimeout(5000); // 5 secondes pour la connexion
        requestFactory.setConnectionRequestTimeout(5000); // 5 secondes pour obtenir une connexion du pool

        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }
}