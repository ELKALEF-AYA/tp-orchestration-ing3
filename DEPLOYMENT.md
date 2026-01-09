# Plateforme E-Commerce Microservices

## 1. Description

Ce projet regroupe une plateforme e-commerce basée sur des microservices (catalogue produits, commandes, utilisateurs) ainsi qu’un frontend React.

---

## 2. Services & ports

| Composant          | Rôle | Port |
|--------------------|------|------|
| Front (React)      | Interface web | `5173` |
| Product Service    | Gestion du catalogue | `8082` |
| Order Service      | Gestion des commandes | `8083` |
| Membership Service | Gestion des utilisateurs | `8081` |
| Prometheus         | Collecte de métriques | `9090` |
| Grafana            | Dashboards | `3000` |

---

## 3. Prérequis

### 3.1 Mode Docker
- Docker + Docker Compose

### 3.2 Mode local (sans Docker)
- Java 21
- Maven 3.8+
- Node.js 18+ / npm

---

## 4. Lancement avec Docker Compose

### 4.1 Démarrer
À la racine du projet :
```bash
cd monitoring/
docker compose up -d --build
```

## 5. Lancement en local (sans Docker)

## 5.1 Product Service
```bash
cd service-product
mvn clean install
mvn spring-boot:run
```

## 5.2 Order Service
```bash
cd service-order
mvn clean install
mvn spring-boot:run
```

## 5.3 Membership Service
```bash
cd ms-membership
mvn clean install
mvn spring-boot:run
```

## 5.4 Frontend
```bash
cd front
npm install
npm run dev
```

## 6. Accès rapides

## 6.1 Frontend
•	http://localhost:5173

## 6.2 Swagger (APIs)
•	Product : http://localhost:8082/swagger-ui/index.html
•	Order : http://localhost:8083/swagger-ui/index.html
•	User : http://localhost:8081/swagger-ui/index.html

## 6.3 Actuator (metrics/health)
•	Product : http://localhost:8082/actuator/health
•	Order : http://localhost:8083/actuator/health
•	User : http://localhost:8081/actuator/health