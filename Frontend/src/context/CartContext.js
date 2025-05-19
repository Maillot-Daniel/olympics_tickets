import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import axios from 'axios';

const CartContext = createContext();

export const CartProvider = ({ children }) => {
    const [cart, setCart] = useState({ items: [] }); // init à un objet vide avec items
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchCart = useCallback(async () => {
        const token = localStorage.getItem('token');
        if (!token) {
            setCart({ items: [] });
            setLoading(false);
            setError('User not authenticated');
            return;
        }

        try {
            setLoading(true);
            setError(null);
            const response = await axios.get('http://localhost:8080/api/cart', {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            setCart(response.data);
        } catch (err) {
            console.error('Error fetching cart:', err);
            setError(err.response?.data?.message || 'Failed to load cart');
            setCart({ items: [] }); // reset cart en cas d'erreur
        } finally {
            setLoading(false);
        }
    }, []);

    const addToCart = async (eventId, offerType, quantity) => {
        const token = localStorage.getItem('token');
        if (!token) {
            setError('User not authenticated');
            return;
        }

        try {
            setLoading(true);
            setError(null);
            await axios.post('/api/cart/items', { eventId, offerType, quantity }, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            await fetchCart(); // refresh cart après ajout
        } catch (err) {
            console.error('Error adding to cart:', err);
            setError(err.response?.data?.message || 'Failed to add item to cart');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCart();
    }, [fetchCart]);

    return (
        <CartContext.Provider
            value={{
                cart,
                loading,
                error,
                addToCart,
                refreshCart: fetchCart,
                isEmpty: !cart || cart.items.length === 0
            }}
        >
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error('useCart must be used within a CartProvider');
    }
    return context;
};
