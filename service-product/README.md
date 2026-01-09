# Service Product - Gestion du Catalogue

## 1. Description

Service de gestion du catalogue de produits pour la plateforme e-commerce.

- **Port** : `8082`

---

## 2. Fonctionnalités

- CRUD complet sur les produits
- Recherche par nom
- Filtrage par catégorie : `ELECTRONICS`, `BOOKS`, `FOOD`, `OTHER`
- Gestion du stock avec opérations : `ADD` / `SUBTRACT` / `SET`
- Produits disponibles (actifs et en stock)
- Validation des données (Bean Validation)
- Health check personnalisé (alerte stock bas)
- Métriques Prometheus (compteurs par catégorie)
- Documentation OpenAPI/Swagger
- Vérification avant suppression :
    - suppression **bloquée** si le produit est référencé dans au moins une commande (appel au service Order)
    - message d’erreur clair renvoyé au frontend (HTTP `409 CONFLICT`)
    - si le service Order est indisponible : HTTP `503 SERVICE_UNAVAILABLE`

---

## 3. Architecture

```
com.example.product/
├── domain/
│   ├── entity/         # Product + ProductCategory enum
│   └── repository/     # ProductRepository (Spring Data JPA)
├── application/
│   ├── dto/            # Request/Response DTOs
│   ├── mapper/         # ProductMapper
│   └── service/        # ProductService (logique métier)
└── infrastructure/
    ├── web/controller/ # ProductController (REST API)
    ├── exception/      # Gestion globale des erreurs (ErrorResponse + Handler)
    ├── health/         # StockHealthIndicator
    └── client/         # OrderServiceClient (appels REST vers Order)
```

---

## 4. Technologies

- Java 21
- Spring Boot 3.5.7
- Spring Data JPA
- H2 Database (en mémoire)
- Lombok
- SpringDoc OpenAPI
- Micrometer + Prometheus
- JUnit 5 + Mockito

---

## 5. Installation et démarrage

### 5.1 Prérequis
- Java 21+
- Maven 3.8+

### 5.2 Compilation
```bash
mvn clean install
```

### 5.3 Démarrage
```bash
mvn spring-boot:run
```

Application disponible sur :
- `http://localhost:8082`

---

## 6. Endpoints REST

### 6.1 Produits

| Méthode | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | Liste tous les produits |
| GET | `/api/v1/products/{id}` | Détail d'un produit |
| POST | `/api/v1/products` | Créer un produit |
| PUT | `/api/v1/products/{id}` | Modifier un produit |
| DELETE | `/api/v1/products/{id}` | Supprimer un produit |
| GET | `/api/v1/products/search?name={name}` | Recherche par nom |
| GET | `/api/v1/products/category/{category}` | Filtrer par catégorie |
| GET | `/api/v1/products/available` | Produits disponibles (actifs + en stock) |
| PATCH | `/api/v1/products/{id}/stock` | Mettre à jour le stock |

### 6.2 Actuator

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | État de santé du service |
| `/actuator/info` | Informations de l'application |
| `/actuator/metrics` | Métriques |
| `/actuator/prometheus` | Export Prometheus |

---

## 7. Documentation API

- Swagger UI : `http://localhost:8082/swagger-ui/index.html`
- OpenAPI JSON : `http://localhost:8082/v3/api-docs`

---

## 8. Exemples d'utilisation

### 8.1 Créer un produit
```bash
curl -X POST http://localhost:8082/api/v1/products   -H "Content-Type: application/json"   -d '{
    "name": "MacBook Pro",
    "description": "Ordinateur portable Apple haute performance",
    "price": 2499.99,
    "stock": 10,
    "category": "ELECTRONICS",
    "imageUrl": "https://example.com/macbook.jpg"
  }'
```

### 8.2 Rechercher des produits
```bash
curl "http://localhost:8082/api/v1/products/search?name=laptop"
```

### 8.3 Mettre à jour le stock (soustraire 2 unités)
```bash
curl -X PATCH http://localhost:8082/api/v1/products/1/stock   -H "Content-Type: application/json"   -d '{
    "quantity": 2,
    "operation": "SUBTRACT"
  }'
```

### 8.4 Filtrer par catégorie
```bash
curl "http://localhost:8082/api/v1/products/category/ELECTRONICS"
```

### 8.5 Supprimer un produit
```bash
curl -X DELETE "http://localhost:8082/api/v1/products/1" -i
```

---

## 9. Base de données

### 9.1 Console H2
- URL : `http://localhost:8082/h2-console`
- JDBC URL : `jdbc:h2:mem:productdb`
- Username : `sa`
- Password : (vide)

### 9.2 Données initiales
Des produits de test sont chargés au démarrage via `src/main/resources/data.sql`.

---

## 10. Health check

Le service expose un indicateur custom pour détecter les produits avec un stock bas (`< 5`).

Test :
```bash
curl "http://localhost:8082/actuator/health"
```

---

## 11. Métriques Prometheus

### 11.1 Compteur de produits créés (toutes catégories)

Nom de métrique : products_total{job="product-service"}

### 11.2 Produits actifs en rupture de stock

Nom de métrique : products_out_of_stock{application="product-service"}

### 11.3 Total des produits existants par catégorie

Nom de métrique : products_existing{job="product-service"}

---

## 12. Règles métier

### 12.1 Validation
- name : 3-100 caractères
- description : 10-500 caractères
- price : > 0 et au plus 2 décimales
- stock : >= 0
- category : valeur parmi `ELECTRONICS`, `BOOKS`, `FOOD`, `OTHER`

### 12.2 Suppression d’un produit
- Un produit ne peut pas être supprimé s’il est référencé dans au moins une commande.
- Vérification via le service Order :
  `GET /api/v1/orders/exists/product/{productId}`
- Cas possibles :
    - `204 NO_CONTENT` : suppression OK
    - `404 NOT_FOUND` : produit introuvable
    - `409 CONFLICT` : produit déjà utilisé dans une commande
    - `503 SERVICE_UNAVAILABLE` : service Order indisponible (impossible de vérifier)

---

## 13. Gestion des erreurs

Le service renvoie une réponse d’erreur standardisée via `GlobalExceptionHandler` (objet `ErrorResponse`).

### 13.1 Exemple : suppression bloquée (produit utilisé)
Réponse typique (HTTP 409) :
```json
{
  "timestamp": "2025-12-14T21:54:29",
  "status": 409,
  "error": "409 CONFLICT",
  "message": "Suppression impossible : le produit est déjà utilisé dans au moins une commande.",
  "path": "/api/v1/products/22"
}
```

### 13.2 Exemple : validation (HTTP 400)
Réponse typique :
```json
{
  "timestamp": "2025-12-14T20:10:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Erreur de validation des données",
  "path": "/api/v1/products",
  "details": [
    "name: Le nom doit contenir entre 3 et 100 caractères"
  ]
}
```

---

## 14. Tests

Exécuter les tests :
- Tests unitaires du ProductService : récupération/recherche, création/mise à jour, filtrage par catégorie, disponibilité (actifs + stock).
- Tests des opérations de stock et de suppression : ADD/SUBTRACT (stock insuffisant) + suppression bloquée si produit utilisé dans une commande (via OrderServiceClient).
```bash
mvn test
```

---

## 15. Configuration inter-services

Le service Product appelle le service Order pour vérifier l’utilisation d’un produit avant suppression.
  
