#  SECURITY.md - Architecture de Sécurité JWT

## Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture JWT](#architecture-jwt)
3. [Flux d'authentification](#flux-dauthentification)
4. [Gestion des clés RSA](#gestion-des-clés-rsa)
5. [Validation des tokens](#validation-des-tokens)
6. [Gestion des erreurs](#gestion-des-erreurs)
7. [Communication inter-services](#communication-inter-services)
8. [Bonnes pratiques](#bonnes-pratiques)

---

## Vue d'ensemble

Cette plateforme e-commerce implémente une **authentification JWT sécurisée** basée sur le chiffrement asymétrique RSA.

### Principes clés

 **Stateless** : Pas de session stockée (JWT est auto-contenu)  
 **Asymétrique** : RSA 2048 bits (clé privée pour signer, publique pour valider)  
 **Expirant** : Token expire après 1 heure  
 **Distribué** : Chaque service valide indépendamment  

### Architecture globale

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ 1. POST /api/users/login
       │    (email + password)
       ▼
┌──────────────────────────────────┐
│  Membership Service (8081)       │
│   Génère JWT                   │
│   Signe avec clé privée RSA    │
└──────┬───────────────────────────┘
       │
       │ 2. Retourne: { token: "..." }
       │
       ▼
┌─────────────────────────────────────┐
│  Client stocke token en localStorage│
└──────┬──────────────────────────────┘
       │
       │ 3. GET /api/products       
       │    Header: Authorization: Bearer <JWT>
       │
       ├─────────────┬─────────────┐
       │             │             │
       ▼             ▼             ▼
┌─────────────┐ ┌─────────────┐ ┌──────────────┐
│  Product    │ │  Order      │ │  Membership  │
│  Service    │ │  Service    │ │  Service     │
│  (8082)     │ │  (8083)     │ │  (8081)      │
│  Valide   │ │  Valide   │ │  Peut      │
│ JWT avec    │ │ JWT avec    │ │ révoquer?    │
│ clé publique│ │ clé publique│ │              │
└─────────────┘ └─────────────┘ └──────────────┘
```

---

## Architecture JWT

### Format du JWT

Un JWT se compose de 3 parties séparées par des points :

```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiIxIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwicm9sZXMiOiJVU0VSIiwiaWF0IjoxNjczMjU3NjAwLCJleHAiOjE2NzMyNjExMDB9.
3F7S8kX9vL2nM1qP4rT6vWx8yZ0aB1cD2eF3...
```

#### 1 **Header** (en Base64)

```json
{
  "alg": "RS256",      // RSA + SHA256
  "typ": "JWT"
}
```

#### 2 **Payload** (Données utilisateur)

```json
{
  "sub": "1",                          // User ID
  "email": "john@example.com",         // Email
  "roles": "USER",                     // Roles
  "iat": 1673257600,                   // Issued At (timestamp)
  "exp": 1673261100                    // Expiration (timestamp)
}
```

**Calcul expiration** : `iat + 3600 secondes = iat + 1 heure`

#### 3 **Signature** (HMAC signé)

```
HMAC_SHA256(
  base64(header) + "." + base64(payload),
  privateKey
)
```

---

## Flux d'authentification

### Étape 1️ : REGISTRATION

```
Client
  ↓
POST /api/users/register
{
  "email": "john@example.com",
  "password": "SecurePass123!",
  "name": "John Doe"
}
  ↓
Membership Service
  ├─ 1. Valider données (email format, pwd strength)
  ├─ 2. Vérifier email unique
  ├─ 3. Hash password avec BCrypt
  │   passwordHash = BCrypt.hash("SecurePass123!", cost=12)
  ├─ 4. Persist en H2 DB
  └─ 5. Retourner 201 Created
  ↓
Client reçoit:
{
  "id": 1,
  "email": "john@example.com",
  "name": "John Doe"
}
```

### Étape 2️ : LOGIN & TOKEN GENERATION

```
Client
  ↓
POST /api/users/login
{
  "email": "john@example.com",
  "password": "SecurePass123!"
}
  ↓
Membership Service (JwtTokenProvider)
  ├─ 1. Charger clé privée RSA
  │   privateKey = Files.read("keys/private_key.pem")
  │
  ├─ 2. Créer claims JWT
  │   JWT.builder()
  │     .subject("1")
  │     .claim("email", "john@example.com")
  │     .claim("roles", "USER")
  │     .issuedAt(now)
  │     .expiration(now + 3600 sec)
  │
  ├─ 3. Signer avec clé privée (RS256)
  │   signature = RSA_SHA256_SIGN(header.payload, privateKey)
  │
  ├─ 4. Encoder en Base64
  │   token = base64(header).base64(payload).base64(signature)
  │
  └─ 5. Retourner token
  ↓
Client reçoit:
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
  ↓
Client stocke le token côté client (localStorage ou mémoire, selon le contexte)```

**Code Java** :

```java
public String generateToken(String userId, String email, String roles) {
    return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000))  // +1h
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact();
}
```

### Étape 3 : UTILISATION DU TOKEN

```
Client (localStorage contient JWT)
  ↓
GET /api/products
Headers: {
  "Authorization": "Bearer eyJhbGciOiJSUzI1NiIs..."
}
  ↓
Product Service (JwtAuthenticationFilter)
  ├─ 1. Extraire token du header
  │   token = authHeader.replace("Bearer ", "")
  │
  ├─ 2. Charger clé publique RSA
  │   publicKey = Files.read("keys/public_key.pem")
  │
  ├─ 3. Parser et valider signature
  │   claims = Jwts.parser()
  │            .verifyWith(publicKey)
  │            .build()
  │            .parseSignedClaims(token)
  │
  ├─ 4. Vérifier expiration
  │   if (claims.exp < System.currentTimeMillis())
  │     throw ExpiredException
  │
  ├─ 5. Extraire claims (userId, email, roles)
  │   userId = claims.getSubject()
  │   email = claims.get("email")
  │   roles = claims.get("roles")
  │
  └─ 6. Créer SecurityContext
      authentication = new JwtAuthenticationToken(userId, email, roles)
      SecurityContextHolder.setContext(authentication)
  ↓
Si valide: Continuez vers le endpoint
Si invalide: Retourner 401 Unauthorized
Si expiré: Retourner 403 Forbidden
```

**Code Java (JwtAuthenticationFilter)** :

```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) 
        throws ServletException, IOException {
    
    try {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // Valider le token
            if (jwtTokenProvider.validateToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);
                String roles = jwtTokenProvider.getRolesFromToken(token);
                
                // Créer authentication
                var authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority(roles))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
        
    } catch (ExpiredJwtException e) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403
        response.getWriter().write("{\"error\": \"Token expired\"}");
    } catch (JwtException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
        response.getWriter().write("{\"error\": \"Invalid token\"}");
    }
}
```

---

## Gestion des clés RSA

### Génération des clés

**Clés déjà générées** (2048 bits, format PEM) :

```bash
# Générer privée key
openssl genrsa -out private_key.pem 2048

# Générer publique key from private
openssl rsa -in private_key.pem -pubout -out public_key.pem
```

### Distribution des clés

```
Projet Root
├── jwt-keys/                    (Master keys - SECRÈTES)
│   ├── private_key.pem         ( PRIVATE - SECRET)
│   └── public_key.pem          ( PUBLIC - peut être partagée)
│
├── ms-membership/
│   └── src/main/resources/keys/
│       ├── private_key.pem     ( Pour signer les tokens)
│       └── public_key.pem      ( Pour validation locale)
│
├── service-product/
│   └── src/main/resources/keys/
│       └── public_key.pem      ( Pour valider tokens reçus)
│
└── service-order/
    └── src/main/resources/keys/
        └── public_key.pem      ( Pour valider tokens reçus)
```

### Configuration dans application.yml

**Membership Service** :

```yaml
app:
  jwt:
    private-key-path: ./keys/private_key.pem
    public-key-path: ./keys/public_key.pem
    expiration: 3600000  # 1 heure en ms
```

**Product & Order Services** :

```yaml
app:
  jwt:
    public-key-path: ./keys/public_key.pem
```

### 🔒 Sécurité des clés

```
 FAIRE
├─ Stocker private_key.pem uniquement sur Membership Service
├─ Utiliser variable d'environnement pour le chemin
├─ Restreindre permissions fichier (600)
├─ Backup sécurisé de la clé privée
└─ Rotation annuelle des clés

 NE PAS FAIRE
├─ Ne pas mettre private_key.pem sur GitHub
├─ Ne pas partager private_key.pem
├─ Ne pas expose le chemin des clés
├─ Ne pas utiliser clé par défaut en production
└─ Ne pas stocker en texte clair
```

---

## Validation des tokens

### Algorithme de validation

```
Token reçu
    ↓
1. Extraire header + payload + signature
    ↓
2. Charger clé publique RSA
    ↓
3. Recalculer signature
   signature_calculée = HMAC_SHA256(header.payload, publicKey)
    ↓
4. Comparer signatures
   if signature_reçue == signature_calculée
      Token valide
   else
      Token falsifié → 401
    ↓
5. Vérifier expiration
   if (exp > now)
      Token non expiré
   else
      Token expiré → 403
    ↓
6. Extraire claims
   userId = payload.sub
   email = payload.email
   roles = payload.roles
```

### Code de validation

```java
public boolean validateToken(String token) {
    try {
        Jwts.parser()
                .verifyWith(publicKey)      // Vérifier signature RSA
                .build()
                .parseSignedClaims(token);  // Parser claims + expiration
        return true;
    } catch (ExpiredJwtException e) {
        log.warn("Token expiré");
        return false;
    } catch (JwtException e) {
        log.warn("Token invalide: {}", e.getMessage());
        return false;
    }
}
```

---

## Gestion des erreurs

###  HTTP 401 Unauthorized

**Quand** : Token invalide, absent, ou malformé

```
Requête: GET /api/products
Header: Authorization: Bearer invalid_token

Réponse: 401 Unauthorized
{
  "status": 401,
  "message": "Invalid or missing authentication token",
  "timestamp": "2024-01-09T12:00:00Z",
  "path": "/api/products"
}
```

**Code** :

```java
if (token == null || !jwtTokenProvider.validateToken(token)) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 401
    response.getWriter().write("{\"error\": \"Invalid token\"}");
}
```

###  HTTP 403 Forbidden

**Quand** : Token expiré

```
Requête: GET /api/products
Header: Authorization: Bearer expired_token

Réponse: 403 Forbidden
{
  "status": 403,
  "message": "Token has expired. Please login again.",
  "timestamp": "2024-01-09T12:00:00Z",
  "path": "/api/products"
}
```

**Code** :

```java
catch (ExpiredJwtException e) {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403
    response.getWriter().write("{\"error\": \"Token expired\"}");
}
```

###  HTTP 500 Internal Server Error

**Quand** : Clés RSA non trouvées, erreur de parsing

```java
catch (IOException | NoSuchAlgorithmException e) {
    log.error("Erreur critique : impossible charger clés RSA", e);
    throw new RuntimeException("Impossible charger clés RSA", e);
}
```

---

## Communication inter-services

### Membership → Aucun appel (Générateur)

Membership ne fait que générer les tokens.

### Product → Membership (Optional)

Product peut appeler Membership pour valider l'existence d'un user :

```java
// ProductService.java
@Component
public class UserServiceClient {
    
    @PostConstruct
    public void addAuthHeader() {
        // Propager le token JWT reçu
        String token = SecurityContextHolder.getContext()
            .getAuthentication()
            .getCredentials();
        
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + token);
            return execution.execute(request, body);
        });
    }
}
```

### Order → Product & Membership

Order Service appelle Product et Membership, propageant le JWT :

```java
@Component
public class ProductServiceClient {
    
    public void decrementStock(Long productId, Integer quantity) {
        // 1. Extraire token du contexte
        String token = extractTokenFromContext();
        
        // 2. Créer requête avec header JWT
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        
        HttpEntity<StockUpdateDTO> request = new HttpEntity<>(dto, headers);
        
        // 3. Appeler Product Service
        restTemplate.exchange(
            "http://product-service:8082/api/products/{id}/decrementStock",
            HttpMethod.PATCH,
            request,
            Void.class,
            productId
        );
    }
}
```

### Gestion des erreurs inter-services

```java
try {
    // Appeler service tiers
    restTemplate.exchange(...);
} catch (HttpClientErrorException.Unauthorized e) {
    // Token invalide/expiré dans le service tiers
    log.error("Service {} rejected our token", serviceName);
    throw new ServiceUnavailableException("Authentication failed");
} catch (HttpServerErrorException e) {
    // Service down ou erreur serveur
    log.error("Service {} error", serviceName);
    throw new ServiceUnavailableException("Service unavailable");
}
```

---

## Bonnes pratiques

###  À FAIRE

```
1. Tokens
    Expiration courte (1h)
    Clé privée sécurisée
    Validation stricte
   
2. Communication
    Propager JWT inter-services
    Gérer erreurs d'auth
    Logs des tentatives échouées
   
3. Infrastructure
    HTTPS en production
    Clés en variables d'env
    Rotation annuelle
   
4. Monitoring
    Alertes sur 401/403
    Logs des accès
    Métriques d'authentification
```

###  À ÉVITER

```
1. Tokens
    Pas de clé symétrique (toujours asymétrique)
    Pas d'expiration infinie
    Pas de storage en localStorage brut
   
2. Clés
    Ne pas versioner private_key.pem
    Ne pas dérober clé publique comme "secrète"
    Ne pas réutiliser clés entre services
   
3. Endpoints
    Ne pas exposer /actuator sans auth
    Ne pas avoir d'endpoint sans sécurité
    Ne pas accepter token en URL query param
```

---

## Tests de sécurité

### Test 1️ : Login valide

```bash
curl -X POST http://localhost:8081/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"SecurePass123!"}'

# Résultat: 200 OK + token
```

### Test 2️ : Requête sans token

```bash
curl http://localhost:8082/api/products

# Résultat: 401 Unauthorized
```

### Test 3 : Token invalide

```bash
curl http://localhost:8082/api/products \
  -H "Authorization: Bearer invalid"

# Résultat: 401 Unauthorized
```

### Test 4️ : Token expiré

```bash
# Attendre 1 heure ou modifier token
curl http://localhost:8082/api/products \
  -H "Authorization: Bearer <EXPIRED_TOKEN>"

# Résultat: 403 Forbidden
```

---


