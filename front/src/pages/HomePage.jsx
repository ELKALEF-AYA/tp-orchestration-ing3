import React from "react";

export default function HomePage() {
    return (
        <div className="container">
            <h1>Plateforme E-Commerce</h1>

            <p className="muted" style={{ fontSize: "1.05rem", marginTop: 6 }}>
                Front-end React connecté aux microservices <strong>Membership</strong>,{" "}
                <strong>Product</strong> et <strong>Order</strong>.
            </p>

            <h2 style={{ marginTop: 28 }}>Fonctionnalités</h2>
            <ul style={{ marginTop: 10, lineHeight: 1.6 }}>
                <li>
                    <strong>Utilisateurs</strong> : créer et lister des utilisateurs
                </li>
                <li>
                    <strong>Produits</strong> : consulter le catalogue, créer un produit,
                    modifier et supprimer un produit
                </li>
                <li>
                    <strong>Panier</strong> : ajouter des produits au panier
                </li>
                <li>
                    <strong>Commandes</strong> : créer des commandes et consulter les commandes existantes
                </li>
            </ul>
        </div>
    );
}