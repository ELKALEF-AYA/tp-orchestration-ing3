import { useCart } from "../context/CartContext";
import { useLocation, useNavigate } from "react-router-dom";
import { useMemo, useState } from "react";
import { createOrder } from "../api/orderApi";
import React from "react";

function useQuery() {
    return new URLSearchParams(useLocation().search);
}

const OrderCreatePage = () => {
    const query = useQuery();
    const userId = query.get("userId");
    const userName = query.get("userName");

    const { cart, total, clearCart } = useCart();
    const [shippingAddress, setShippingAddress] = useState("");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const navigate = useNavigate();

    const trimmedAddress = useMemo(() => shippingAddress.trim(), [shippingAddress]);

    const isAddressValid = trimmedAddress.length >= 10;

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        if (!userId) {
            setError("Aucun utilisateur sélectionné.");
            return;
        }

        if (cart.length === 0) {
            setError("Panier vide, impossible de créer une commande.");
            return;
        }

        if (!isAddressValid) {
            setError("Adresse invalide : minimum 10 caractères (sans espaces).");
            return;
        }

        const orderRequest = {
            userId: Number(userId),
            shippingAddress: trimmedAddress,
            items: cart.map((i) => ({
                productId: i.product.id,
                quantity: i.quantity,
            })),
        };

        try {
            setSubmitting(true);
            const created = await createOrder(orderRequest);
            clearCart();
            navigate(`/orders/${created.id}`);
        } catch (err) {
            setError("Erreur lors de la création de la commande.");
        } finally {
            setSubmitting(false);
        }
    };

    const canSubmit = !!userId && cart.length > 0 && isAddressValid && !submitting;

    return (
        <div style={{ maxWidth: 900, margin: "0 auto", padding: "24px 16px" }}>
            <h2>Créer une commande</h2>

            {userId && (
                <p>
                    Pour l'utilisateur <strong>{userName}</strong> (ID: {userId})
                </p>
            )}

            {error && <p className="error">{error}</p>}

            <p>
                Montant du panier : <strong>{total} €</strong>
            </p>

            <form onSubmit={handleSubmit}>
                <div style={{ margin: "16px 0" }}>
                    <label style={{ display: "block", fontWeight: 600, marginBottom: 8 }}>
                        Adresse de livraison <span className="req">*</span>
                    </label>

                    <textarea
                        value={shippingAddress}
                        onChange={(e) => setShippingAddress(e.target.value)}
                        rows={3}
                        placeholder="Ex: 2 avenue Foch, 75016 Paris"
                        style={{ width: "100%", maxWidth: 600 }}
                    />

                    {shippingAddress.length > 0 && !isAddressValid && (
                        <p style={{ marginTop: 6, fontSize: 13, color: "#b45309" }}>
                            Minimum 10 caractères (hors espaces).
                        </p>
                    )}
                </div>

                <button type="submit" disabled={!canSubmit}>
                    {submitting ? "Création..." : "Créer la commande"}
                </button>
            </form>
        </div>
    );
};

export default OrderCreatePage;