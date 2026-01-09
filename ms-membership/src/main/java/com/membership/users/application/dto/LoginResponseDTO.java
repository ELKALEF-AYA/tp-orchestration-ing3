package com.membership.users.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;
    private String tokenType;
    private long expiresIn;
    private String userId;
    private String email;

    public static LoginResponseDTO of(String token, long expiresInMs, String userId, String email) {
        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresInMs / 1000)
                .userId(userId)
                .email(email)
                .build();
    }
}