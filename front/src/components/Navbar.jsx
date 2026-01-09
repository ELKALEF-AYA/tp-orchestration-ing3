import React from "react";
import { Link, NavLink } from "react-router-dom";
import { useCart } from "../context/CartContext";

const Navbar = () => {
    const { cart } = useCart();

    return (
        <nav className="navbar">
            <Link to="/" className="navbar-brand">
                Plateforme E-Commerce
            </Link>

            <div className="navbar-links">
                <NavLink to="/users">Utilisateurs</NavLink>
                <NavLink to="/users/new">Créer un utilisateur</NavLink>

                <NavLink to="/products">Produits</NavLink>
                <NavLink to="/products/new">Créer un produit</NavLink>

                <NavLink to="/orders">Commandes</NavLink>
                <NavLink to="/cart">Panier ({cart.length})</NavLink>
            </div>
        </nav>
    );
};

export default Navbar;