# Service ORDER – Microservice de gestion des commandes

## 1. Description générale

Le **Service ORDER** est un microservice **Spring Boot** responsable de la gestion des commandes dans la plateforme e-commerce microservices.

Il permet :

- Créer des commandes pour un utilisateur existant
- Gérer les articles d’une commande
- Calculer automatiquement le montant total
- Mettre à jour le statut des commandes
- Annuler une commande
- Communiquer avec les services **MEMBERSHIP (User)** et **PRODUCT**

**Port d’exécution : `8083`**

---

## 2. Responsabilités du service

- Gestion complète des commandes (**Order**)
- Gestion des articles de commande (**OrderItem**)
- Vérification de l’existence et de l’état des utilisateurs
- Vérification de la disponibilité et du stock des produits
- Déduction automatique du stock lors de la création d’une commande
- Respect des règles métier imposées

---

## 3. Modèle de données

### 3.1 Entité `Order`

| Champ | Type | Description |
|------|------|-------------|
| `id` | `Long` | Identifiant de la commande |
| `userId` | `Long` | Identifiant de l’utilisateur |
| `orderDate` | `LocalDateTime` | Date de la commande |
| `status` | `Enum` | `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED` |
| `totalAmount` | `BigDecimal` | Montant total calculé |
| `shippingAddress` | `String` | Adresse de livraison |
| `createdAt` | `LocalDateTime` | Date de création |
| `updatedAt` | `LocalDateTime` | Date de mise à jour |

### 3.2 Entité `OrderItem`

| Champ | Type | Description |
|------|------|-------------|
| `id` | `Long` | Identifiant de l’article |
| `order` | `Order` | Référence vers la commande |
| `productId` | `Long` | Identifiant du produit |
| `productName` | `String` | Nom du produit (copie) |
| `quantity` | `Integer` | Quantité commandée |
| `unitPrice` | `BigDecimal` | Prix unitaire |
| `subtotal` | `BigDecimal` | `quantity × unitPrice` |

**Relation :** Une commande contient plusieurs `OrderItem` (**OneToMany**).

---

## 4. Endpoints REST exposés

### 4.1 Commandes

| Méthode | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/orders` | Liste toutes les commandes |
| `GET` | `/api/v1/orders/{id}` | Détail d’une commande |
| `POST` | `/api/v1/orders` | Créer une commande |
| `PUT` | `/api/v1/orders/{id}/status` | Mettre à jour le statut |
| `DELETE` | `/api/v1/orders/{id}` | Annuler une commande |
| `GET` | `/api/v1/orders/user/{userId}` | Commandes d’un utilisateur |
| `GET` | `/api/v1/orders/status/{status}` | Commandes par statut |

---

## 5. Création d’une commande

Lors de la création d’une commande :

1. Vérification que l’utilisateur existe et est actif (appel au service **MEMBERSHIP**).
2. Vérification que chaque produit existe, est actif et a un stock suffisant (appel au service **PRODUCT**).
3. Déduction du stock de chaque produit via l’endpoint `PATCH /products/{id}/stock`.
4. Calcul automatique du total de la commande.
5. Sauvegarde de la commande et des articles associés.

**Contraintes :**
- Une commande doit contenir **au moins un article**.

---

## 6. Règles métier implémentées

- Une commande `DELIVERED` ou `CANCELLED` ne peut plus être modifiée.
- Le stock des produits est décrémenté lors de la création de la commande.
- Une commande sans articles est refusée.
- Un produit en rupture de stock empêche la création de la commande.
- Un utilisateur inexistant ou inactif empêche la création de la commande.

---

## 7. Communication inter-services

Le service **ORDER** communique avec :

### 7.1 Service `MEMBERSHIP` (User)
- Vérification de l’existence et de l’état actif de l’utilisateur.

### 7.2 Service `PRODUCT`
- Récupération des informations produit
- Vérification du stock
- Mise à jour du stock via `PATCH`

La communication est réalisée via `RestTemplate` configuré avec `HttpComponentsClientHttpRequestFactory` pour supporter la méthode `PATCH`.

---

## 8. Gestion des erreurs

- Gestion centralisée des exceptions avec `@ControllerAdvice`
- Messages clairs retournés au client en cas d’erreur métier
- Gestion des erreurs lorsque les services externes sont indisponibles

---

## 9. Base de données

- Base de données **H2 en mémoire**
- Création automatique des tables au démarrage

### Accès à la console H2
- URL : `/h2-console`
- JDBC URL : `jdbc:h2:mem:orderdb`

---

## 10. Health Check et Monitoring

- Health check personnalisé vérifiant la disponibilité :
    - du service **MEMBERSHIP**
    - du service **PRODUCT**
- Endpoints **Actuator** exposés pour **Prometheus**

---

## 11. Métriques Prometheus

## 11.1 Compteur de commandes par statut

Nom de métrique : orders_by_status{job="ms-order"}

## 11.2 Montant total des commandes du jour

Nom de métrique : orders_total_amount_today{job="order-service"}

## 11.3 Total des commandes

Nom de métrique : orders_total{job="order-service"}

## 12. Lancement du service

### Prérequis
- Java 21+
- Maven 3.8+

### Commandes
```bash
mvn clean spring-boot:run
```

Le service démarre sur : `http://localhost:8083`

---

## 13. Tests

- Tests unitaires du OrderService : récupération (toutes / par ID / par user / par statut), création de commande (vérification user actif, stock produit, mise à jour stock, calcul du total).
- Tests des règles métier : changement de statut et annulation (commande non modifiable si DELIVERED), vérification “produit utilisé” via existsByProductId, et compteur Micrometer incrémenté quand une commande passe à DELIVERED.

Exécuter les tests :
```bash
mvn test
```
