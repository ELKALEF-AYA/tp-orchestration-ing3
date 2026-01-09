package com.membership.users.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.private-key-path:./keys/private_key.pem}")
    private String privateKeyPath;

    @Value("${app.jwt.public-key-path:./keys/public_key.pem}")
    private String publicKeyPath;

    @Value("${app.jwt.expiration:3600000}")
    private long jwtExpirationMs;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void initializeKeys() {
        try {
            this.privateKey = loadPrivateKey(privateKeyPath);
            this.publicKey = loadPublicKey(publicKeyPath);
            log.info("Clés RSA chargées avec succès");
        } catch (Exception e) {
            log.error("Erreur lors du chargement des clés RSA", e);
            throw new RuntimeException("Impossible de charger les clés RSA", e);
        }
    }

    public String generateToken(String userId, String email, String roles) {
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Token invalide: {}", e.getMessage());
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getEmailFromToken(String token) {
        return (String) getClaimsFromToken(token).get("email");
    }

    public String getRolesFromToken(String token) {
        return (String) getClaimsFromToken(token).get("roles");
    }

    public boolean isTokenExpired(String token) {
        return getClaimsFromToken(token).getExpiration().before(new Date());
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PrivateKey loadPrivateKey(String keyPath) throws Exception {
        String keyContent = loadKeyFile(keyPath);
        keyContent = keyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(keyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String keyPath) throws Exception {
        String keyContent = loadKeyFile(keyPath);
        keyContent = keyContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(keyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private String loadKeyFile(String keyPath) throws IOException {
        if (keyPath.startsWith("classpath:")) {
            String classpathLocation = keyPath.replace("classpath:", "");
            try (var is = getClass().getClassLoader().getResourceAsStream(classpathLocation)) {
                if (is == null) {
                    throw new IOException("Fichier non trouvé dans le classpath: " + classpathLocation);
                }
                return new String(is.readAllBytes());
            }
        } else {
            return new String(Files.readAllBytes(Paths.get(keyPath)));
        }
    }

}