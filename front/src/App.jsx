import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import Navbar from "./components/Navbar";

import HomePage from "./pages/HomePage";
import UsersListPage from "./pages/UsersListPage";
import UserCreatePage from "./pages/UserCreatePage";
import ProductsPage from "./pages/ProductsPage";
import CartPage from "./pages/CartPage";
import OrderCreatePage from "./pages/OrderCreatePage";
import OrdersListPage from "./pages/OrdersListPage";
import OrderDetailsPage from "./pages/OrderDetailsPage";
import CreateProductPage from "./pages/CreateProductPage";
import EditProductPage from "./pages/EditProductPage";

function App() {
    return (
        <>
            <Navbar />
            <main className="container">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/users" element={<UsersListPage />} />
                    <Route path="/users/new" element={<UserCreatePage />} />
                    <Route path="/products/new" element={<CreateProductPage />} />
                    <Route path="/products" element={<ProductsPage />} />
                    <Route path="/products/:id/edit" element={<EditProductPage />} />
                    <Route path="/cart" element={<CartPage />} />
                    <Route path="/orders/new" element={<OrderCreatePage />} />
                    <Route path="/orders" element={<OrdersListPage />} />
                    <Route path="/orders/:id" element={<OrderDetailsPage />} />
                    <Route path="*" element={<Navigate to="/" />} />
                </Routes>
            </main>
        </>
    );
}

export default App;
