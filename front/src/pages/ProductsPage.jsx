import React, { useEffect, useState } from "react";
import { getAllProducts } from "../api/productApi";
import ProductCard from "../components/ProductCard";

const ProductsPage = () => {
    const [products, setProducts] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    const load = async () => {
        try {
            const data = await getAllProducts();
            setProducts(data || []);
        } catch (e) {
            setError(e.response?.data?.message || "Erreur lors du chargement des produits");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, []);

    if (loading) return <p>Chargement des produits...</p>;

    return (
        <div className="container">
            <h2>Catalogue produits</h2>

            {error && <p className="error">{error}</p>}

            <div className="grid">
                {products.length > 0 ? (
                    products.map((p) => (
                        <ProductCard
                            key={p.id}
                            product={p}
                            onDeleted={(id) => setProducts((prev) => prev.filter((x) => x.id !== id))}
                        />
                    ))
                ) : (
                    <p>Aucun produit disponible.</p>
                )}
            </div>
        </div>
    );
};

export default ProductsPage;