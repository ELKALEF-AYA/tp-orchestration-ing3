import React, { createContext, useContext, useState } from "react";

export const CartContext = createContext();

export const CartProvider = ({ children }) => {
    const [cart, setCart] = useState([]);


    const addToCart = (product) => {
        setCart((prev) => {
            const existing = prev.find((item) => item.product.id === product.id);

            if (existing) {

                return prev.map((item) =>
                    item.product.id === product.id
                        ? { ...item, quantity: item.quantity + 1 }
                        : item
                );
            }


            return [...prev, { product, quantity: 1 }];
        });
    };


    const removeFromCart = (productId) => {
        setCart((prev) => prev.filter((item) => item.product.id !== productId));
    };


    const clearCart = () => setCart([]);

    const total = cart.reduce(
        (sum, item) => sum + item.product.price * item.quantity,
        0
    );

    return (
        <CartContext.Provider value={{ cart, addToCart, removeFromCart, clearCart, total }}>
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => useContext(CartContext);
