import { useEffect, useState } from "react";
import { getAllOrders } from "../api/orderApi";
import { useNavigate } from "react-router-dom";
import OrderStatusBadge from "../components/OrderStatusBadge";
import React from "react";
const OrdersListPage = () => {
    const [orders, setOrders] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const load = async () => {
        try {
            const data = await getAllOrders();
            setOrders(data);
        } catch (e) {
            setError(e.response?.data?.message || "Erreur lors du chargement des commandes");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, []);

    if (loading) return <p>Chargement des commandes...</p>;

    return (
        <div>
            <h2>Commandes</h2>

            {error && <p className="error">{error}</p>}

            <table className="table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Utilisateur</th>
                    <th>Date</th>
                    <th>Montant</th>
                    <th>Statut</th>
                    <th></th>
                </tr>
                </thead>

                <tbody>
                {orders.length > 0 ? (
                    orders.map((o) => (
                        <tr key={o.id}>
                            <td>{o.id}</td>
                            <td>{o.userId}</td>

                            <td>
                                {o.orderDate
                                    ? new Date(o.orderDate).toLocaleString()
                                    : "—"}
                            </td>

                            <td>{o.totalAmount} €</td>

                            <td>
                                <OrderStatusBadge status={o.status} />
                            </td>

                            <td>
                                <button onClick={() => navigate(`/orders/${o.id}`)}>
                                    Détails
                                </button>
                            </td>
                        </tr>
                    ))
                ) : (
                    <tr>
                        <td colSpan="6">Aucune commande.</td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
};

export default OrdersListPage;
