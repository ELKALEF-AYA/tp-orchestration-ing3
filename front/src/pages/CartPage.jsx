import React from "react";
import { useCart } from "../context/CartContext";
import { useNavigate } from "react-router-dom";

const CartPage = () => {
    const { cart, removeFromCart, total, clearCart } = useCart();
    const navigate = useNavigate();

    const handleCreateOrder = () => {
        navigate("/users");
    };

    return (
        <div className="container product-form">
            <h2>Panier</h2>

            {cart.length === 0 ? (
                <p>Votre panier est vide.</p>
            ) : (
                <>
                    <table className="table">
                        <thead>
                        <tr>
                            <th>Produit</th>
                            <th>Prix</th>
                            <th>Quantité</th>
                            <th>Sous-total</th>
                            <th></th>
                        </tr>
                        </thead>

                        <tbody>
                        {cart.map((i) => (
                            <tr key={i.product.id}>
                                <td>{i.product.name}</td>
                                <td>{i.product.price} €</td>
                                <td>{i.quantity}</td>
                                <td>{i.product.price * i.quantity} €</td>

                                <td>
                                    <button className="danger" onClick={() => removeFromCart(i.product.id)}>
                                        Retirer
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    <p><strong>Total : {total} €</strong></p>

                    <div className="form-actions">
                        <button onClick={clearCart}>Vider le panier</button>
                        <button onClick={handleCreateOrder}>
                            Choisir un utilisateur et créer la commande
                        </button>
                    </div>
                </>
            )}
        </div>
    );
};

export default CartPage;