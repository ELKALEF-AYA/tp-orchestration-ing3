import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getOrderById, updateOrderStatus, cancelOrder } from "../api/orderApi";
import OrderStatusBadge from "../components/OrderStatusBadge";
import React from "react";

const OrderDetailsPage = () => {
    const { id } = useParams();
    const [order, setOrder] = useState(null);
    const [error, setError] = useState("");
    const [updating, setUpdating] = useState(false);

    const load = async () => {
        try {
            const data = await getOrderById(id);
            setOrder(data);
        } catch (e) {
            setError("Erreur lors du chargement de la commande.");
        }
    };

    useEffect(() => {
        load();
    }, [id]);

    const handleChangeStatus = async (newStatus) => {
        setUpdating(true);
        setError("");

        try {
            await updateOrderStatus(id, newStatus);
            await load();
        } catch (e) {
            setError(e.response?.data?.message || "Erreur lors de la mise à jour du statut.");
        } finally {
            setUpdating(false);
        }
    };

    const handleCancel = async () => {
        setUpdating(true);
        setError("");

        try {
            await cancelOrder(id);
            await load();
        } catch (e) {
            setError(e.response?.data?.message || "Erreur lors de l'annulation.");
        } finally {
            setUpdating(false);
        }
    };

    if (!order) return <p>Chargement...</p>;

    const status = String(order.status || "").toUpperCase();

    const isModifiable = status !== "DELIVERED" && status !== "CANCELLED";
    const canConfirm = status !== "SHIPPED";
    const canShip = status === "PENDING" || status === "CONFIRMED" || status === "SHIPPED";
    const canCancel = status === "PENDING" || status === "CONFIRMED" || status === "SHIPPED";

    return (
        <div>
            <h2>Détails de la commande #{order.id}</h2>

            {error && <p className="error">{error}</p>}

            <p>Utilisateur ID : <strong>{order.userId}</strong></p>
            <p>Adresse : <strong>{order.shippingAddress}</strong></p>

            <p>
                Date : {new Date(order.orderDate).toLocaleString()} <br />
                Montant : <strong>{order.totalAmount} €</strong>
            </p>

            <p>
                Statut : <OrderStatusBadge status={order.status} />
            </p>

            <h3>Articles</h3>

            <table className="table">
                <thead>
                <tr>
                    <th>Produit</th>
                    <th>Quantité</th>
                    <th>Prix unitaire</th>
                    <th>Sous-total</th>
                </tr>
                </thead>
                <tbody>
                {order.items?.map((it) => (
                    <tr key={it.id}>
                        <td>{it.productName}</td>
                        <td>{it.quantity}</td>
                        <td>{it.unitPrice} €</td>
                        <td>{it.subtotal} €</td>
                    </tr>
                ))}
                </tbody>
            </table>

            {isModifiable && (
                <div>
                    <h3>Actions</h3>

                    <button
                        disabled={updating || !canConfirm}
                        onClick={() => handleChangeStatus("CONFIRMED")}
                    >
                        Marquer comme CONFIRMÉE
                    </button>{" "}

                    <button
                        disabled={updating || !canShip}
                        onClick={() => handleChangeStatus("SHIPPED")}
                    >
                        Marquer comme EXPÉDIÉE
                    </button>{" "}

                    <button
                        disabled={updating || !canCancel}
                        onClick={handleCancel}
                    >
                        Annuler la commande
                    </button>
                </div>
            )}
        </div>
    );
};

export default OrderDetailsPage;