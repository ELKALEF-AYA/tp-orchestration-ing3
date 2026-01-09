import { useEffect, useState } from "react";
import { getAllUsers } from "../api/userApi";
import { useNavigate } from "react-router-dom";
import React from "react";
const UsersListPage = () => {
    const [users, setUsers] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const load = async () => {
            try {
                const data = await getAllUsers();
                setUsers(data);
            } catch (e) {
                setError(
                    e.response?.data?.message ||
                    "Erreur lors du chargement des utilisateurs"
                );
            } finally {
                setLoading(false);
            }
        };
        load();
    }, []);

    if (loading) return <p>Chargement des utilisateurs...</p>;

    return (
        <div>
            <h2>Utilisateurs</h2>

            {error && <p className="error">{error}</p>}

            <table className="table">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Nom complet</th>
                    <th>Email</th>
                    <th>Actif</th>
                    <th>Actions</th>
                </tr>
                </thead>

                <tbody>
                {users.map((u) => (
                    <tr key={u.id}>
                        <td>{u.id}</td>
                        <td>{u.firstName} {u.lastName}</td>
                        <td>{u.email}</td>
                        <td>{u.active ? "Oui" : "Non"}</td>
                        <td>
                            <button
                                onClick={() =>
                                    navigate(
                                        `/orders/new?userId=${u.id}&userName=${encodeURIComponent(
                                            u.firstName + " " + u.lastName
                                        )}`
                                    )
                                }
                            >
                                Créer une commande
                            </button>
                        </td>
                    </tr>
                ))}

                {users.length === 0 && !loading && (
                    <tr>
                        <td colSpan="5">Aucun utilisateur.</td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
};

export default UsersListPage;
