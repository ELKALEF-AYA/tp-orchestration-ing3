import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getProductById, updateProduct } from "../api/productApi";

const CATEGORIES = [
    { value: "ELECTRONICS", label: "Électronique" },
    { value: "BOOKS", label: "Livres" },
    { value: "FOOD", label: "Alimentation" },
    { value: "OTHER", label: "Autre" },
];

export default function EditProductPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [form, setForm] = useState(null); // null = pas encore chargé
    const [loading, setLoading] = useState(true);
    const [serverError, setServerError] = useState("");

    useEffect(() => {
        const load = async () => {
            try {
                const p = await getProductById(id);
                setForm({
                    name: p.name ?? "",
                    description: p.description ?? "",
                    price: p.price ?? "",
                    stock: p.stock ?? 0,
                    category: p.category ?? "ELECTRONICS",
                    imageUrl: p.imageUrl ?? "",
                    active: p.active ?? true,
                });
            } catch (e) {
                setServerError("Impossible de charger le produit.");
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [id]);

    const onChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const onSubmit = async (e) => {
        e.preventDefault();
        setServerError("");

        try {
            const payload = {
                ...form,
                stock: Number(form.stock),
                imageUrl: form.imageUrl?.trim() ? form.imageUrl.trim() : null,
            };
            await updateProduct(id, payload);
            navigate("/products");
        } catch (e) {
            setServerError("Erreur lors de la modification.");
        }
    };

    if (loading) return <div className="container">Chargement...</div>;
    if (!form) return <div className="container">{serverError}</div>;

    return (
        <div className="container">
            <h1>Modifier le produit</h1>

            {serverError && (
                <div style={{ margin: "12px 0", padding: 12, borderRadius: 8, background: "#ffe9e9" }}>
                    {serverError}
                </div>
            )}

            <form className="product-form" onSubmit={onSubmit}>
                <div className="form-grid">
                    <div className="form-group">
                        <label>Nom <span className="req">*</span></label>
                        <input name="name" value={form.name} onChange={onChange} required />
                    </div>

                    <div className="form-group">
                        <label>Stock <span className="req">*</span></label>
                        <input name="stock" type="number" min="0" value={form.stock} onChange={onChange} required />
                    </div>

                    <div className="form-group form-span-2">
                        <label>Description <span className="req">*</span></label>
                        <textarea name="description" value={form.description} onChange={onChange} required />
                    </div>

                    <div className="form-group">
                        <label>Prix (€) <span className="req">*</span></label>
                        <input name="price" type="number" min="0.01" step="0.01" value={form.price} onChange={onChange} required />
                    </div>

                    <div className="form-group">
                        <label>Catégorie <span className="req">*</span></label>
                        <select name="category" value={form.category} onChange={onChange} required>
                            {CATEGORIES.map((c) => (
                                <option key={c.value} value={c.value}>{c.label}</option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group form-span-2">
                        <label>Image URL</label>
                        <input name="imageUrl" value={form.imageUrl} onChange={onChange} placeholder="https://..." />
                    </div>

                    <div className="form-group form-span-2">
                        <label className="checkbox-inline">
                            <input name="active" type="checkbox" checked={form.active} onChange={onChange} />
                            Produit actif
                        </label>
                    </div>
                </div>

                <div className="form-actions">
                    <button type="submit">Enregistrer</button>
                    <button type="button" onClick={() => navigate("/products")}>
                        Annuler
                    </button>
                </div>
            </form>
        </div>
    );
}