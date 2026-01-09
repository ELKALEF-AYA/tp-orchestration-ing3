# Document d’Architecture Technique (DAT)
## Plateforme E-Commerce Microservices

---

## 1. Vue d’ensemble de la plateforme

Cette plateforme e-commerce est basée sur une architecture **microservices**.
Chaque fonctionnalité métier est isolée dans un service indépendant afin
d’améliorer la maintenabilité, l’évolutivité et la résilience du système.

La plateforme permet :
- La gestion des utilisateurs
- La gestion du catalogue de produits
- La gestion des commandes

Chaque microservice expose une API REST et possède sa propre base de données.

---

## 2. Architecture globale

L’architecture repose sur trois microservices principaux :
- **Membership Service** : gestion des utilisateurs
- **Product Service** : gestion des produits
- **Order Service** : gestion des commandes

Les services communiquent entre eux via des appels REST synchrones.
Un système de monitoring basé sur Prometheus et Grafana permet de suivre l’état
et les métriques de la plateforme.

*Un schéma d’architecture global est fourni (réalisé avec Draw.io).*


---

## 3. Description des microservices

### 3.1 Service Membership (Port 8081)

Responsabilités :
- Gestion des utilisateurs (CRUD)
- Validation des données
- Exposition des health checks
- Exposition des métriques Prometheus

Ce service est consommé par le service Order afin de vérifier l’existence
d’un utilisateur lors de la création d’une commande.

---

### 3.2 Service Product (Port 8082)

Responsabilités :
- Gestion du catalogue de produits
- Gestion du stock
- Recherche et filtrage des produits
- Validation des règles métier

Règles métier principales :
- Le stock ne peut pas être négatif
- Le prix doit être strictement positif
- Un produit ne peut pas être supprimé s’il est associé à une commande

Le service expose également :
- Un health check personnalisé pour détecter les produits en stock faible
- Des métriques sur les produits créés par catégorie

---

### 3.3 Service Order (Port 8083)

Responsabilités :
- Création et gestion des commandes
- Calcul automatique du montant total
- Gestion du cycle de vie des commandes

Règles métier principales :
- Une commande doit contenir au moins un produit
- Une commande livrée ou annulée ne peut plus être modifiée
- Le stock des produits est décrémenté lors de la création d’une commande

Le service communique avec :
- Membership Service pour vérifier l’existence d’un utilisateur
- Product Service pour vérifier la disponibilité des produits

---

## 4. Choix technologiques

Les technologies utilisées sont :
- **Java 17**
- **Spring Boot**
- **Spring Web** pour les API REST
- **Spring Data JPA** pour l’accès aux données
- **H2 Database** (base embarquée pour simplifier le développement)
- **Maven** pour la gestion des dépendances
- **Docker** pour la conteneurisation
- **Prometheus & Grafana** pour le monitoring

Ces choix permettent un développement rapide, structuré et conforme aux bonnes
pratiques des architectures microservices.

---

## 5. Communication inter-services

La communication entre microservices se fait via des appels REST synchrones,
en utilisant **RestTemplate** ou **WebClient**.

Des contrôles sont mis en place pour :
- Gérer les erreurs lorsque l’un des services est indisponible
- Retourner des messages d’erreur clairs au client

---

## 6. Gestion des données

Chaque microservice possède sa **propre base de données**, conformément au
principe d’isolation des microservices.

Aucune base de données n’est partagée entre les services, ce qui garantit :
- Une indépendance totale
- Une meilleure évolutivité
- Une meilleure résilience

---

## 7. Gestion des erreurs et résilience

La gestion des erreurs est centralisée grâce à l’utilisation de
`@ControllerAdvice`.

Les erreurs possibles (données invalides, service indisponible, règles métier)
sont correctement interceptées et retournées avec des codes HTTP appropriés.

Les health checks permettent de détecter rapidement les problèmes de disponibilité.

---

## 8. Monitoring et health checks

Chaque microservice expose :
- Un endpoint `/actuator/health`
- Un endpoint `/actuator/prometheus`

Ces endpoints sont utilisés par Prometheus pour collecter les métriques,
qui peuvent ensuite être visualisées dans Grafana.
