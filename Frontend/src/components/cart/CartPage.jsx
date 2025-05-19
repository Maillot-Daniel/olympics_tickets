import React from 'react';
import { useCart } from '../../context/CartContext';
import { loadStripe } from '@stripe/stripe-js';
import axios from 'axios';

const stripePromise = loadStripe('pk_test_votreClePublique');

function CartPage() {
    const { cart, loading, fetchCart } = useCart();

    const removeItem = async (itemId) => {
        try {
            await axios.delete(`/api/cart/items/${itemId}`);
            await fetchCart();
        } catch (error) {
            console.error('Error removing item:', error);
        }
    };

    const handleCheckout = async () => {
        try {
            const response = await axios.post('/api/payment/create-checkout-session', {
                cartId: cart.id
            });
            
            const stripe = await stripePromise;
            await stripe.redirectToCheckout({
                sessionId: response.data.sessionId
            });
        } catch (error) {
            console.error('Error during checkout:', error);
        }
    };

    if (loading) return <div>Loading cart...</div>;
    if (!cart) return <div>No active cart</div>;

    return (
        <div className="cart-container">
            <h2>Your Cart</h2>
            
            {cart.items.length === 0 ? (
                <p>Your cart is empty</p>
            ) : (
                <>
                    <div className="cart-items">
                        {cart.items.map((item) => (
                            <div key={`${item.eventId}-${item.offerType}`} className="cart-item">
                                <h4>{item.eventTitle}</h4>
                                <p>Offer: {item.offerType}</p>
                                <p>Quantity: {item.quantity}</p>
                                <p>Price: €{(item.totalPrice).toFixed(2)}</p>
                                <button onClick={() => removeItem(item.id)}>Remove</button>
                            </div>
                        ))}
                    </div>
                    
                    <div className="cart-total">
                        <h3>Total: €{cart.total.toFixed(2)}</h3>
                    </div>
                    
                    <button 
                        onClick={handleCheckout}
                        className="checkout-button"
                    >
                        Proceed to Checkout
                    </button>
                </>
            )}
        </div>
    );
}

export default CartPage;