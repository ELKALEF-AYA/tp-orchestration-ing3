import React from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { deleteProduct } from "../api/productApi";

const ProductCard = ({ product, onDeleted }) => {
    const { addToCart } = useCart();
    const navigate = useNavigate();

    const handleDelete = async () => {
        if (!window.confirm(`Supprimer "${product.name}" ?`)) return;

        try {
            await deleteProduct(product.id);
            onDeleted?.(product.id);
        } catch (e) {
            alert(e.response?.data?.message || "Erreur lors de la suppression.");
        }
    };

    const CATEGORY_LABELS = {
        ELECTRONICS: "Électronique",
        BOOKS: "Livres",
        FOOD: "Alimentation",
        OTHER: "Autre",
    };

    return (
        <div className="card product-card">
      <span className={`category-badge ${product.category?.toLowerCase()}`}>
        {CATEGORY_LABELS[product.category] ?? product.category}
      </span>

            <h3>{product.name}</h3>
            <p className="muted">{product.description}</p>
            <p><strong>{product.price} €</strong></p>
            <p>Stock: {product.stock}</p>

            <div className="card-actions">
                <button
                    disabled={!product.active || product.stock <= 0}
                    onClick={() => addToCart(product)}
                >
                    {product.active && product.stock > 0 ? "Ajouter au panier" : "Indisponible"}
                </button>

                <button
                    type="button"
                    className="secondary"
                    disabled={!product.active || product.stock <= 0}
                    onClick={() => navigate(`/products/${product.id}/edit`)}
                >
                    Modifier
                </button>

                <button type="button" className="danger" onClick={handleDelete}>
                    Supprimer
                </button>
            </div>
        </div>
    );
};

export default ProductCard;