import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createProduct } from "../api/productApi";

const CATEGORIES = [
    { value: "ELECTRONICS", label: "Électronique" },
    { value: "BOOKS", label: "Livres" },
    { value: "FOOD", label: "Alimentation" },
    { value: "OTHER", label: "Autre" },
];

export default function CreateProductPage() {
    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: "",
        description: "",
        price: "",
        stock: 0,
        category: "ELECTRONICS",
        imageUrl: "",
        active: true,
    });

    const [errors, setErrors] = useState({});
    const [serverError, setServerError] = useState("");
    const [loading, setLoading] = useState(false);

    const onChange = (e) => {
        const { name, value, type, checked } = e.target;
        setForm((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const validate = () => {
        const e = {};

        const name = form.name.trim();
        const desc = form.description.trim();

        if (!name) e.name = "Le nom est obligatoire";
        else if (name.length < 3) e.name = "Min 3 caractères";
        else if (name.length > 100) e.name = "Max 100 caractères";

        if (!desc) e.description = "La description est obligatoire";
        else if (desc.length < 10) e.description = "Min 10 caractères";
        else if (desc.length > 500) e.description = "Max 500 caractères";

        if (form.price === "" || form.price === null) e.price = "Le prix est obligatoire";
        else if (Number(form.price) <= 0) e.price = "Le prix doit être > 0";

        if (form.stock === "" || form.stock === null) e.stock = "Le stock est obligatoire";
        else if (Number(form.stock) < 0) e.stock = "Le stock ne peut pas être négatif";

        if (!form.category) e.category = "La catégorie est obligatoire";

        if (form.imageUrl && form.imageUrl.length > 255) e.imageUrl = "Max 255 caractères";

        setErrors(e);
        return Object.keys(e).length === 0;
    };

    const extractBackendErrors = (err) => {
        const data = err?.response?.data;

        if (data?.errors && typeof data.errors === "object") return data.errors;

        if (Array.isArray(data?.fieldErrors)) {
            const mapped = {};
            data.fieldErrors.forEach((fe) => {
                if (fe.field && fe.message) mapped[fe.field] = fe.message;
            });
            return mapped;
        }

        if (typeof data?.message === "string") return { _global: data.message };

        return null;
    };

    const onSubmit = async (e) => {
        e.preventDefault();
        setServerError("");

        if (!validate()) return;

        setLoading(true);
        try {
            const payload = {
                name: form.name.trim(),
                description: form.description.trim(),
                price: form.price,
                stock: Number(form.stock),
                category: form.category,
                imageUrl: form.imageUrl.trim() ? form.imageUrl.trim() : null,
                active: Boolean(form.active),
            };

            await createProduct(payload);
            navigate("/products");
        } catch (err) {
            const backendFieldErrors = extractBackendErrors(err);
            if (backendFieldErrors) {
                setErrors((prev) => ({ ...prev, ...backendFieldErrors }));
                if (backendFieldErrors._global) setServerError(backendFieldErrors._global);
            } else {
                setServerError("Erreur lors de la création du produit.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <h1>Créer un produit</h1>

            {serverError && (
                <div style={{ margin: "12px 0", padding: 12, borderRadius: 8, background: "#ffe9e9" }}>
                    {serverError}
                </div>
            )}

            <form className="product-form" onSubmit={onSubmit}>
                <div className="form-grid">
                    <div className="form-group">
                        <label>
                            Nom <span className="req">*</span>
                        </label>
                        <input
                            name="name"
                            value={form.name}
                            onChange={onChange}
                            required
                        />
                        {errors.name && <p className="field-error">{errors.name}</p>}
                    </div>

                    <div className="form-group">
                        <label>
                            Stock <span className="req">*</span>
                        </label>
                        <input
                            name="stock"
                            type="number"
                            min="0"
                            value={form.stock}
                            onChange={onChange}
                            required
                        />
                        {errors.stock && <p className="field-error">{errors.stock}</p>}
                    </div>

                    <div className="form-group form-span-2">
                        <label>
                            Description <span className="req">*</span>
                        </label>
                        <textarea
                            name="description"
                            value={form.description}
                            onChange={onChange}
                            required
                        />
                        {errors.description && <p className="field-error">{errors.description}</p>}
                    </div>

                    <div className="form-group">
                        <label>
                            Prix (€) <span className="req">*</span>
                        </label>
                        <input
                            name="price"
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={form.price}
                            onChange={onChange}
                            required
                        />
                        {errors.price && <p className="field-error">{errors.price}</p>}
                    </div>

                    <div className="form-group">
                        <label>
                            Catégorie <span className="req">*</span>
                        </label>
                        <select
                            name="category"
                            value={form.category}
                            onChange={onChange}
                            required
                        >
                            {CATEGORIES.map((c) => (
                                <option key={c.value} value={c.value}>
                                    {c.label}
                                </option>
                            ))}
                        </select>
                        {errors.category && <p className="field-error">{errors.category}</p>}
                    </div>

                    <div className="form-group form-span-2">
                        <label>Image URL</label>
                        <input
                            name="imageUrl"
                            value={form.imageUrl}
                            onChange={onChange}
                            placeholder="https://..."
                        />
                        {errors.imageUrl && <p className="field-error">{errors.imageUrl}</p>}
                    </div>

                    <div className="form-group form-span-2">
                        <label className="checkbox-inline">
                            <input
                                name="active"
                                type="checkbox"
                                checked={form.active}
                                onChange={onChange}
                            />
                            <span>Produit actif</span>
                        </label>
                    </div>
                </div>

                <div className="form-actions">
                    <button type="submit" disabled={loading}>
                        {loading ? "Création..." : "Créer"}
                    </button>
                    <button type="button" className="secondary" onClick={() => navigate("/products")} disabled={loading}>
                        Annuler
                    </button>
                </div>
            </form>
        </div>
    );
}