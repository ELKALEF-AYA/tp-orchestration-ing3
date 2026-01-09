package com.membership.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour recevoir les informations d'un utilisateur depuis le service User.
 * Simplifié - contient uniquement les champs nécessaires.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean active;
    private LocalDateTime createdAt;
}
