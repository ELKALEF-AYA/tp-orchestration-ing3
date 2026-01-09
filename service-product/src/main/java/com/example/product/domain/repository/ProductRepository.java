package com.example.product.domain.repository;

import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.Product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Product.
 *
 * Spring Data JPA génère automatiquement les implémentations.
 * Méthodes de recherche personnalisées suivant les conventions de nommage.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Recherche des produits par nom (contient, insensible à la casse).
     *
     * @param name Le nom ou partie du nom à rechercher
     * @return Liste des produits correspondants
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Filtre les produits par catégorie.
     *
     * @param category La catégorie à filtrer
     * @return Liste des produits de cette catégorie
     */
    List<Product> findByCategory(ProductCategory category);

    /**
     * Récupère tous les produits disponibles en stock (stock > stock).
     *
     * @param stock seuil (ex: 0) => produits avec stock > 0
     * @return Liste des produits en stock
     */
    List<Product> findByStockGreaterThan(Integer stock);

    /**
     * Récupère tous les produits actifs.
     *
     * @return Liste des produits actifs
     */
    List<Product> findByActiveTrue();

    /**
     * Compte le nombre de produits avec un stock bas (< seuil).
     * Utilisé pour le health check.
     *
     * @param threshold Le seuil de stock bas
     * @return Nombre de produits avec stock bas
     */
    long countByStockLessThan(Integer threshold);

    /**
     * Compte le nombre de produits par catégorie.
     * Utilisé pour les métriques.
     *
     * @param category La catégorie
     * @return Nombre de produits dans cette catégorie
     */
    long countByCategory(ProductCategory category);

    /**
     * Compte le nombre de produits en rupture de stock (stock = valeur donnée)
     * et actifs uniquement.
     * Utilisé pour une métrique.
     *
     * @param stock Valeur de stock à compter (ex: 0 pour rupture de stock)
     * @return Nombre de produits actifs avec ce stock
     */
    long countByStockAndActiveTrue(Integer stock);

    /**
     * Vérifie si un produit avec ce nom existe déjà.
     *
     * @param name Le nom du produit
     * @return true si le produit existe
     */
    boolean existsByName(String name);

    /**
     * Vérifie si un produit avec ce nom existe (en excluant l'ID donné).
     * Utile pour les mises à jour.
     *
     * @param name Le nom du produit
     * @param id   L'ID à exclure
     * @return true si le produit existe
     */
    boolean existsByNameAndIdNot(String name, Long id);
}