# DOCKER.md - Guide de Deployment Docker
> ⚠️ Note Docker Hub  
> En raison des limitations du plan Docker Hub Personal (1 seul repository privé),
> les images ont été publiées en public afin de permettre la correction du TP.
> Le passage en privé est possible sur un plan payant.

## Table des matières

1. [Prérequis](#prérequis)
2. [Architecture Docker](#architecture-docker)
3. [Build local](#build-local)
4. [Docker Hub](#docker-hub)
5. [Déploiement](#déploiement)
6. [Commandes essentielles](#commandes-essentielles)
7. [Dépannage](#dépannage)

---

## 1. Prérequis

### Outils requis

- Docker : v20.10+
- Docker Compose : v1.29+
- Java : 21+
- Maven : 3.8+
- Git

### Vérification d'installation

```bash
docker --version
docker compose version
java -version
mvn --version
```

---

## 2. Architecture Docker

### 2.1 Structure des Dockerfiles

Chaque service utilise un Dockerfile multi-stage pour optimiser la taille de l'image :

**Stage 1 : Compilation Maven**

```dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY ../Downloads/pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
```

**Stage 2 : Runtime**
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/keys ./keys
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Avantages :
- Images petites (500-600MB au lieu de 1.5GB)
- Compilation en conteneur (pas besoin Maven local)
- Clés RSA incluses dans l'image
- Prêt pour production

### 2.2 docker-compose.yml

Structure complète d'orchestration :

```yaml
version: "3.9"

services:
  membership-service:
    build:
      context: ./ms-membership
      dockerfile: Dockerfile
    container_name: membership-service
    ports:
      - "8081:8081"
    networks:
      - app-network

  product-service:
    build:
      context: ./service-product
      dockerfile: Dockerfile
    container_name: product-service
    ports:
      - "8082:8082"
    depends_on:
      - membership-service
    networks:
      - app-network

  order-service:
    build:
      context: ./service-order
      dockerfile: Dockerfile
    container_name: order-service
    ports:
      - "8083:8083"
    depends_on:
      - membership-service
      - product-service
    networks:
      - app-network

  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - app-network

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
    depends_on:
      - prometheus
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

Services orchestrés :
- 3 microservices Spring Boot
- Prometheus pour les métriques
- Grafana pour la visualisation
- Network bridge pour la communication interne

---

## 3. Build local

### Étape 1 : Créer le dossier docker/

```bash
mkdir -p docker
cd docker
```

### Étape 2 : Copier les scripts d'automation

Placer les trois scripts dans le dossier docker/ :
- build-all.sh : Compile et build les images
- publish-all.sh : Tag et publie sur Docker Hub
- deploy.sh : Pull depuis Docker Hub et lance docker-compose

### Étape 3 : Rendre les scripts exécutables

```bash
chmod +x docker/build-all.sh
chmod +x docker/publish-all.sh
chmod +x docker/deploy.sh
```

### Étape 4 : Compiler et builder les images

```bash
cd docker
bash build-all.sh
```

Processus automatisé :
1. Maven compile chaque service
2. Docker build les images multi-stage
3. Images créées avec le tag 1.0

### Étape 5 : Vérifier les images créées

```bash
docker images

# Résultat attendu :
# ecommerce-membership:1.0
# ecommerce-product:1.0
# ecommerce-order:1.0
```

### Étape 6 : Test local avant Docker Hub

```bash
# Lancer les services
docker-compose up -d

# Attendre 15 secondes
sleep 15

# Vérifier l'état
docker-compose ps

# Test health check
curl http://localhost:8081/actuator/health
# Résultat : {"status":"UP"}

# Arrêter
docker-compose down
```

---

## 4. Docker Hub

### 4.1 Créer les repositories

1. Aller sur https://hub.docker.com
2. Se connecter ou créer un compte
3. Cliquer sur "Create" → "Create Repository"

Créer 3 repositories :
- ecommerce-membership (Public ou Private)
- ecommerce-product (Public ou Private)
- ecommerce-order (Public ou Private)

### 4.2 S'authentifier à Docker Hub

```bash
docker login
# Username: votre_username
# Password: votre_token (générer sur hub.docker.com/settings/security)
```

### 4.3 Tagger les images locales

```bash
docker tag ecommerce-membership:1.0 votre-username/ecommerce-membership:1.0
docker tag ecommerce-product:1.0 votre-username/ecommerce-product:1.0
docker tag ecommerce-order:1.0 votre-username/ecommerce-order:1.0
```

### 4.4 Publier les images

```bash
cd docker
bash publish-all.sh
# Ou manuellement :
docker push votre-username/ecommerce-membership:1.0
docker push votre-username/ecommerce-product:1.0
docker push votre-username/ecommerce-order:1.0
```

Processus de publication :
1. Vérification du login Docker
2. Tagging des images avec le username
3. Push vers Docker Hub
4. Affichage des URLs de chaque repository

### 4.5 Vérifier sur Docker Hub

Allez sur https://hub.docker.com/r/votre-username et vérifiez que les 3 repositories sont présents.

---

## 5. Déploiement

### 5.1 Option A : Build et déploiement local

```bash
# À la racine du projet
docker-compose up -d --build

# Services démarrent automatiquement
sleep 15

# Vérifier l'état
docker-compose ps
```

### 5.2 Option B : Pull depuis Docker Hub et déployer

```bash
cd docker
bash deploy.sh
# Ou manuellement :
docker pull votre-username/ecommerce-membership:1.0
docker pull votre-username/ecommerce-product:1.0
docker pull votre-username/ecommerce-order:1.0

docker-compose up -d
```

### 5.3 Accès aux services

Une fois déployés, les services sont disponibles sur :

```
Frontend : http://localhost:5173

Membership Service : http://localhost:8081
  API : http://localhost:8081/api/v1/users
  Health : http://localhost:8081/actuator/health
  Swagger : http://localhost:8081/swagger-ui/index.html

Product Service : http://localhost:8082
  API : http://localhost:8082/api/v1/products
  Health : http://localhost:8082/actuator/health
  Swagger : http://localhost:8082/swagger-ui/index.html

Order Service : http://localhost:8083
  API : http://localhost:8083/api/v1/orders
  Health : http://localhost:8083/actuator/health
  Swagger : http://localhost:8083/swagger-ui/index.html

Prometheus : http://localhost:9090
  Métriques : http://localhost:9090/metrics

Grafana : http://localhost:3000
  Utilisateur : admin
  Mot de passe : admin
```

---

## 6. Commandes essentielles

### Gestion des conteneurs

Arrêter tous les services :
```bash
docker-compose down
```

Arrêter et supprimer les volumes (attention : perte de données) :
```bash
docker-compose down -v
```

Redémarrer les services :
```bash
docker-compose restart
```

Redémarrer un service spécifique :
```bash
docker-compose restart membership-service
```

### Gestion des logs

Afficher les logs en temps réel :
```bash
docker-compose logs -f
```

Logs d'un service spécifique :
```bash
docker-compose logs -f membership-service
docker-compose logs -f product-service
docker-compose logs -f order-service
```

Voir les logs récents (dernières 100 lignes) :
```bash
docker-compose logs --tail=100 membership-service
```

### Gestion des images

Lister les images locales :
```bash
docker images
```

Supprimer une image :
```bash
docker rmi ecommerce-membership:1.0
```

Supprimer les images non utilisées :
```bash
docker image prune
```

Supprimer toutes les images orphelines :
```bash
docker image prune -a
```

### Inspection des conteneurs

Voir les conteneurs en cours :
```bash
docker-compose ps
```

Voir les statistiques de ressources :
```bash
docker stats
```

Exécuter une commande dans un conteneur :
```bash
docker exec membership-service ls -la /app/keys
docker exec membership-service cat /app/application.yml
```

### Inspection du réseau

Lister les networks :
```bash
docker network ls
```

Voir les détails d'un network :
```bash
docker network inspect app-network
```

Tester la connectivité entre conteneurs :
```bash
docker exec membership-service ping product-service
```

---

## 7. Dépannage

### Problème : Port déjà utilisé

Symptôme : "Address already in use"

Solution :
```bash
# Identifier le processus
lsof -i :8081  # Pour le port 8081

# Tuer le processus
kill -9 <PID>

# Ou modifier le port dans docker-compose.yml
# "8081:8081" → "8091:8081"
```

### Problème : Container refuse de démarrer

Solution :
```bash
# Voir les logs d'erreur
docker-compose logs membership-service

# Chercher les messages d'erreur liés à :
# - Clés RSA manquantes
# - Permissions fichier incorrectes
# - Port déjà utilisé
# - Configuration manquante
```

### Problème : Les services ne communiquent pas

Solution :
```bash
# Vérifier le network
docker network inspect app-network

# Tester la connexion
docker exec product-service ping membership-service

# Si pong : connexion OK
# Si échec : problème de network

# Vérifier les URLs inter-services
# Elles doivent être : http://service-name:port
# Pas : http://localhost:port (localhost ne fonctionne pas entre conteneurs)
```

### Problème : Clés RSA introuvables

Solution :
```bash
# Vérifier les clés dans le conteneur
docker exec membership-service ls -la /app/keys

# Si absent, ajouter dans Dockerfile :
# COPY src/main/resources/keys ./keys

# Reconstruire l'image
docker-compose build --no-cache membership-service
docker-compose up -d
```

### Problème : Erreur JWT "Invalid signature"

Cause : Clés publiques non synchronisées entre services

Solution :
```bash
# Vérifier que les clés publiques sont identiques
docker exec membership-service cat /app/keys/public_key.pem > /tmp/membership_pub.pem
docker exec product-service cat /app/keys/public_key.pem > /tmp/product_pub.pem

# Comparer les fichiers
diff /tmp/membership_pub.pem /tmp/product_pub.pem
# Si différent : copier la bonne clé publique
```

### Problème : Prometheus ne scrape pas les métriques

Solution :
```bash
# Vérifier prometheus.yml existe
ls -la monitoring/prometheus.yml

# Vérifier que les endpoints sont accessibles
curl http://localhost:8081/actuator/prometheus

# Vérifier que les targets sont up
# Allez sur http://localhost:9090/targets
```

---

## 8. Optimisations

### Réduire la taille des images

Utiliser jre-slim :
```dockerfile
FROM eclipse-temurin:21-jre-slim
```

Supprimer les artefacts de build :
```dockerfile
RUN rm -rf /app/target /app/.m2
```

### Limiter les ressources

Dans docker-compose.yml :
```yaml
services:
  membership-service:
    deploy:
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M
```

### Variables d'environnement

Passer des variables au démarrage :
```bash
export JWT_EXPIRATION=3600000
export DB_URL=jdbc:h2:mem:ecommerce
docker-compose up -d
```

Ou utiliser un fichier .env :
```
JWT_EXPIRATION=3600000
DB_URL=jdbc:h2:mem:ecommerce
```

---

## 9. Checklist de déploiement

Avant de soumettre :

Prérequis :
- Docker Desktop lancé
- Maven et Java 21 installés
- Clés RSA présentes dans les services

Build :
- Dossier docker/ créé
- 3 scripts (build-all.sh, publish-all.sh, deploy.sh) copiés
- bash docker/build-all.sh exécuté avec succès
- docker images montre 3 images ecommerce:1.0

Docker Hub :
- 3 repositories créés sur Docker Hub
- docker login exécuté
- bash docker/publish-all.sh exécuté avec succès
- Images visibles sur hub.docker.com

Déploiement :
- docker-compose up -d exécuté
- docker-compose ps montre 7 services UP
- curl http://localhost:8081/actuator/health retourne 200 OK
- Accès à http://localhost:5173 fonctionne

Validation :
- Tests Postman : Login OK
- Tests Postman : GET /products avec token → 200 OK
- Tests Postman : GET /products sans token → 401 Unauthorized
- Tests Postman : POST /orders avec token → 201 Created
- Prometheus scrape les métriques
- Grafana affiche les dashboards

Git :
- Tous les fichiers committés (docker/, SECURITY.md, DOCKER.md)
- Dernière push avant la deadline

---

**Document Docker TP2 - Généré 09/01/2026**
