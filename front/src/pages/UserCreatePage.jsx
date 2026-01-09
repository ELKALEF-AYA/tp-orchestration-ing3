import React, { useState } from "react";
import { createUser } from "../api/userApi";
import { useNavigate } from "react-router-dom";

const UserCreatePage = () => {
    const [form, setForm] = useState({
        firstName: "",
        lastName: "",
        email: "",
    });

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setForm((prev) => ({
            ...prev,
            [e.target.name]: e.target.value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            await createUser({
                firstName: form.firstName.trim(),
                lastName: form.lastName.trim(),
                email: form.email.trim().toLowerCase(),
            });

            navigate("/users");
        } catch (err) {
            setError(
                err.response?.data?.message || "Erreur lors de la création de l'utilisateur"
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <h1>Créer un utilisateur</h1>

            {error && <p className="error">{error}</p>}

            <form className="product-form" onSubmit={handleSubmit}>
                <div className="form-grid">
                    <div className="form-group">
                        <label>
                            Prénom <span className="req">*</span>
                        </label>
                        <input
                            name="firstName"
                            value={form.firstName}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>
                            Nom <span className="req">*</span>
                        </label>
                        <input
                            name="lastName"
                            value={form.lastName}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className="form-group form-span-2">
                        <label>
                            Email <span className="req">*</span>
                        </label>
                        <input
                            name="email"
                            type="email"
                            value={form.email}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>

                <div className="form-actions">
                    <button type="submit" disabled={loading}>
                        {loading ? "Création..." : "Créer"}
                    </button>

                    <button type="button" className="secondary" onClick={() => navigate("/users")}>
                        Annuler
                    </button>
                </div>
            </form>
        </div>
    );
};

export default UserCreatePage;