package com.membership.users.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.membership.users.application.dto.LoginRequestDTO;
import com.membership.users.application.dto.LoginResponseDTO;
import com.membership.users.application.service.AuthenticationService;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API d'authentification JWT")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Login utilisateur",
            description = "Authentifie un utilisateur et retourne un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login réussi",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Données invalides",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Email ou mot de passe invalide",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé",
                    content = @Content)
    })
    @PostMapping(value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {

        log.info("Requête de login pour: {}", loginRequest.getEmail());

        LoginResponseDTO response = authenticationService.login(loginRequest);

        return ResponseEntity.ok(response);
    }
}