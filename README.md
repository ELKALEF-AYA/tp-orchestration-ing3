# README.md - Plateforme E-Commerce Microservices TP2

## 1. Description

Plateforme e-commerce basée sur des microservices avec authentification JWT sécurisée et conteneurisation Docker. Le projet comprend trois services Spring Boot, un frontend React, et un stack monitoring complet (Prometheus/Grafana).

**Objectifs TP2 :**
- Sécurisation JWT avec chiffrement RSA asymétrique
- Dockerisation complète (Dockerfiles multi-stage + docker-compose)
- Publication sur Docker Hub
- Documentation sécurité et déploiement
- Tests de sécurité complets

---

## 2. Services et ports

| Service | Rôle | Port | Protocole |
|---------|------|------|-----------|
| Frontend (React) | Interface web | 5173 | HTTP |
| Membership Service | Authentification, gestion utilisateurs | 8081 | HTTP |
| Product Service | Catalogue produits, stocks | 8082 | HTTP |
| Order Service | Gestion des commandes | 8083 | HTTP |
| Prometheus | Collecte de métriques | 9090 | HTTP |
| Grafana | Dashboards de monitoring | 3000 | HTTP |

---

## 3. Prérequis

### 3.1 Mode Docker (Recommandé)

- Docker v20.10+
- Docker Compose v1.29+

### 3.2 Mode local (sans Docker)

- Java 21 (Oracle JDK ou Eclipse Temurin)
- Maven 3.8+
- Node.js 18+
- npm ou yarn

### 3.3 Vérification d'installation

```bash
docker --version
docker compose version
java -version
mvn --version
node --version
npm --version
```

---

## 4. Architecture JWT et Sécurité

### 4.1 Authentification

La plateforme implémente une authentification JWT avec chiffrement asymétrique RSA 2048 bits.

Flux d'authentification :
1. Client envoie POST /api/v1/auth/login (email + password)
2. Membership Service signe le token avec la clé privée RSA
3. Client reçoit le JWT dans la réponse
4. Client propague le JWT dans le header Authorization: Bearer <token>
5. Product et Order Services valident la signature avec la clé publique RSA

### 4.2 Clés RSA

Les clés RSA sont stockées dans le dossier keys/ de chaque service :

```
ms-membership/src/main/resources/keys/
├── private_key.pem         (SECRET - Membership seulement)
└── public_key.pem

service-product/src/main/resources/keys/
└── public_key.pem

service-order/src/main/resources/keys/
└── public_key.pem
```

### 4.3 Validation des réponses

- 200 OK : Token valide et fonctionnalité accessible
- 401 Unauthorized : Token absent, invalide ou malformé
- 403 Forbidden : Token expiré (expiration 1 heure)
- 500 Internal Server Error : Erreur serveur (clés RSA manquantes, etc.)

Pour plus de détails, consulter SECURITY.md

---

## 5. Lancement avec Docker

### 5.1 Build et déploiement

À la racine du projet :

```bash
# Approche 1 : Build et déployer localement
docker-compose up -d --build

# Approche 2 : Utiliser les scripts d'automation
cd docker
bash build-all.sh     # Compile et crée les images
bash publish-all.sh   # Publie sur Docker Hub
bash deploy.sh        # Pull depuis Docker Hub et lance
```

### 5.2 Arrêter les services

```bash
docker-compose down

# Ou avec suppression des volumes
docker-compose down -v
```

### 5.3 Vérifier l'état

```bash
docker-compose ps
docker-compose logs -f
```

---

## 6. Lancement en local (sans Docker)

### 6.1 Membership Service (Port 8081)

```bash
cd ms-membership
mvn clean package -DskipTests
mvn spring-boot:run
```

### 6.2 Product Service (Port 8082)

```bash
cd service-product
mvn clean package -DskipTests
mvn spring-boot:run
```

### 6.3 Order Service (Port 8083)

```bash
cd service-order
mvn clean package -DskipTests
mvn spring-boot:run
```

### 6.4 Frontend (Port 5173)

```bash
cd front
npm install
npm run dev
```

---

## 7. Accès aux services

### 7.1 Interfaces web

- Frontend : http://localhost:5173
- Prometheus : http://localhost:9090
- Grafana : http://localhost:3000 (admin / admin)

### 7.2 API Swagger (Documentation interactive)

- Membership Service : http://localhost:8081/swagger-ui/index.html
- Product Service : http://localhost:8082/swagger-ui/index.html
- Order Service : http://localhost:8083/swagger-ui/index.html

### 7.3 Health checks (Actuator)

- Membership : http://localhost:8081/actuator/health
- Product : http://localhost:8082/actuator/health
- Order : http://localhost:8083/actuator/health

### 7.4 Métriques Prometheus

- Product : http://localhost:8082/actuator/prometheus
- Order : http://localhost:8083/actuator/prometheus
- Membership : http://localhost:8081/actuator/prometheus

---

## 8. Tests de sécurité

### 8.1 Collection Postman

Une collection Postman complète est fournie : `postmanCollection/platform-secured.json`

Import dans Postman :
1. Ouvrir Postman
2. File → Import
3. Sélectionner postmanCollection/platform-secured.json
4. Collection importée avec tous les tests

### 8.2 Tests de base (curl)

#### Test 1 : Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"SecurePass123!"}'

# Résultat : 200 OK + token JWT
```

#### Test 2 : Requête sans token (devrait être rejetée)

```bash
curl http://localhost:8082/api/v1/products

# Résultat : 401 Unauthorized
```

#### Test 3 : Requête avec token valide

```bash
curl http://localhost:8082/api/v1/products \
  -H "Authorization: Bearer <TOKEN_DU_LOGIN>"

# Résultat : 200 OK + liste des produits
```

#### Test 4 : Token invalide

```bash
curl http://localhost:8082/api/v1/products \
  -H "Authorization: Bearer invalid_token"

# Résultat : 401 Unauthorized
```

#### Test 5 : Token expiré (après 1 heure)

```bash
curl http://localhost:8082/api/v1/products \
  -H "Authorization: Bearer <EXPIRED_TOKEN>"

# Résultat : 403 Forbidden
```

---

## 9. Documentation

### 9.1 SECURITY.md

Documentation complète de l'architecture JWT :
- Format des tokens (Header, Payload, Signature)
- Flux d'authentification (3 étapes)
- Génération et distribution des clés RSA
- Validation des tokens
- Gestion des erreurs (401, 403, 500)
- Communication inter-services sécurisée
- Bonnes pratiques sécurité

### 9.2 DOCKER.md

Guide complet de déploiement Docker :
- Architecture Dockerfile multi-stage
- Configuration docker-compose.yml
- Build local des images
- Publication sur Docker Hub
- Déploiement (options locales et depuis Docker Hub)
- Commandes Docker essentielles (20+)
- Dépannage (7 problèmes courants)
- Optimisations
- Checklist de déploiement

---

## 10. Docker Hub

### 10.1 Repositories publics

Les images sont publiées sur Docker Hub sous le username `elkaya20` :

- elkaya20/ecommerce-membership:1.0
- elkaya20/ecommerce-product:1.0
- elkaya20/ecommerce-order:1.0

### 10.2 Pull et exécution

```bash
docker pull elkaya20/ecommerce-membership:1.0
docker pull elkaya20/ecommerce-product:1.0
docker pull elkaya20/ecommerce-order:1.0

docker-compose up -d
```



## 11. Arborescence du projet

```
tp-orchestration-ing3/
├── docker/                          (Scripts d'automation Docker)
│   ├── build-all.sh                (Compile et build les images)
│   ├── publish-all.sh              (Tag et publie sur Docker Hub)
│   └── deploy.sh                   (Pull et lance docker-compose)
│
├── ms-membership/                   (Service d'authentification)
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/java/...           (Code JWT, authentification)
│   └── src/main/resources/keys/    (Clés RSA privée/publique)
│
├── service-product/                 (Service catalogue)
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/java/...
│   └── src/main/resources/keys/    (Clé publique RSA)
│
├── service-order/                   (Service commandes)
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/java/...
│   └── src/main/resources/keys/    (Clé publique RSA)
│
├── front/                           (Frontend React)
│   ├── package.json
│   ├── src/
│   ├── public/
│   └── .env                         (Configuration API)
│
├── monitoring/                      (Configuration Prometheus)
│   └── prometheus.yml
│
├── postmanCollection/               (Tests Postman)
│   └── platform-secured.json
│
├── docker-compose.yml               (Orchestration 7 services)
│
├── SECURITY.md                      (Documentation JWT)
├── DOCKER.md                        (Guide déploiement Docker)
├── README.md                        (Ce fichier)
└── .gitignore
```

---

## 12. Variables d'environnement

### 12.1 Configuration application.yml

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

### 12.2 Frontend (.env)

```
REACT_APP_API_BASE_URL=http://localhost:8081
REACT_APP_PRODUCT_API_URL=http://localhost:8082
REACT_APP_ORDER_API_URL=http://localhost:8083
```

---

## 13. Livrables TP2

Date limite : Lundi 12 janvier 2026 à minuit
Email de soumission : rkarra.okad@gmail.com

### 13.1 Code source

- Trois services Spring Boot sécurisés (JWT + RSA)
- Dossier docker/ avec scripts d'automation
- Frontend React intégré
- Dockerfiles multi-stage optimisés

### 13.2 Documentation

- SECURITY.md : Architecture JWT complète
- DOCKER.md : Guide déploiement et commandes
- README.md : Ce fichier (overview et quickstart)

### 13.3 Tests

- Collection Postman : platform-secured.json
- 5 scénarios de sécurité testés
- Tous les tests passent (401, 403, 200)

### 13.4 Infrastructure

- 3 images Docker publiées sur Docker Hub
- docker-compose.yml fonctionnel
- Scripts build-all.sh, publish-all.sh, deploy.sh opérationnels

---

## 14. Commandes Docker essentielles

### Démarrage et arrêt

```bash
docker-compose up -d              # Démarrer tous les services
docker-compose down               # Arrêter les services
docker-compose restart            # Redémarrer
```

### Logs et debugging

```bash
docker-compose logs -f            # Logs en temps réel
docker-compose logs -f membership-service  # Logs d'un service
docker exec <container> bash      # Accès au shell du conteneur
```

### Gestion des images

```bash
docker images                     # Lister les images
docker build -t <name>:1.0 .     # Builder une image
docker push <username>/<name>:1.0 # Publier sur Docker Hub
```

### Inspection

```bash
docker-compose ps                 # État des conteneurs
docker stats                      # Ressources utilisées
docker network inspect app-network # Détails du réseau
```

---

## 15. Dépannage

### Port déjà utilisé

```bash
lsof -i :8081
kill -9 <PID>
```

### Services ne communiquent pas

```bash
docker exec product-service ping membership-service
docker network inspect app-network
```

### Clés RSA introuvables

```bash
docker exec membership-service ls -la /app/keys
# Vérifier que COPY src/main/resources/keys ./keys existe dans Dockerfile
```

### Token invalide ou expiré

Consulter SECURITY.md section "Gestion des erreurs"

---

## 16. Architecture globale

```
Client (Frontend React, Postman)
     |
     | Authorization: Bearer <JWT>
     |
     v
┌─────────────────────────────────┐
│  Product Service (8082)         │
│  - Valide JWT avec clé publique │
│  - Catalogue produits           │
│  - Appelle Membership optionnel  │
└─────────────────────────────────┘
     ^
     |
     | Propague JWT
     |
┌─────────────────────────────────┐
│  Order Service (8083)           │
│  - Valide JWT avec clé publique │
│  - Gestion commandes            │
│  - Appelle Product et Membership│
└─────────────────────────────────┘
     ^
     |
     | Retourne JWT
     |
┌─────────────────────────────────┐
│  Membership Service (8081)      │
│  - Génère JWT avec clé privée   │
│  - Authentification utilisateur  │
│  - Gestion utilisateurs         │
└─────────────────────────────────┘
     ^
     |
Client envoie credentials
```

---

## 17. Bonnes pratiques

### À faire

- Utiliser HTTPS en production
- Stocker les clés RSA en variables d'environnement
- Valider strictement les JWT (signature + expiration)
- Logger les tentatives d'authentification échouées
- Monitorer les erreurs 401/403
- Rotation annuelle des clés RSA

### À éviter

- Ne pas committer private_key.pem sur Git
- Ne pas utiliser clés symétriques (toujours RSA asymétrique)
- Ne pas exposer /actuator sans authentification
- Ne pas accepter tokens en URL (toujours en header)
- Ne pas stocker les tokens en localStorage brut

---

## 18. Support et contact

En cas de problème, consulter :
1. SECURITY.md pour les problèmes JWT
2. DOCKER.md pour les problèmes déploiement
3. Logs : docker-compose logs -f <service>
---

