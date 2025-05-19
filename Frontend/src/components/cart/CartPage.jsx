import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { loadStripe } from '@stripe/stripe-js';

const stripePromise = loadStripe('pk_test_votreClePublique');

function CartPage() {
  const [cart, setCart] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  // Récupération du panier et des événements depuis le localStorage
  useEffect(() => {
    const savedCart = localStorage.getItem('cart');
    setCart(savedCart ? JSON.parse(savedCart) : []);

    const fetchEvents = async () => {
      try {
        const res = await axios.get('http://localhost:8080/api/events');
        setEvents(Array.isArray(res.data.content) ? res.data.content : []);
      } catch (err) {
        console.error("Erreur lors du chargement des événements", err);
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  const saveCart = (newCart) => {
    setCart(newCart);
    localStorage.setItem('cart', JSON.stringify(newCart));
  };

  const removeItem = (index) => {
    const itemToRemove = cart[index];
    const newCart = [...cart];
    newCart.splice(index, 1);
    saveCart(newCart);
  };

  const getTotal = () => cart.reduce((sum, item) => sum + item.total, 0).toFixed(2);

  const handleCheckout = async () => {
    if (cart.length === 0) return;

    try {
      const response = await axios.post('http://localhost:8080/api/payment/create-checkout-session', {
        cartItems: cart
      });

      const stripe = await stripePromise;
      await stripe.redirectToCheckout({ sessionId: response.data.sessionUrl });
    } catch (error) {
      console.error('Erreur pendant le paiement :', error);
      alert('Une erreur est survenue lors du paiement');
    }
  };

  if (loading) return <div>Chargement du panier...</div>;

  return (
    <div className="cart-container">
      <h2>Votre Panier</h2>

      {cart.length === 0 ? (
        <p>Votre panier est vide</p>
      ) : (
        <>
          <div className="cart-items">
            {cart.map((item, index) => (
              <div key={index} className="cart-item">
                <h4>{item.title}</h4>
                <p>Offre : {item.offer}</p>
                <p>Quantité : {item.quantity}</p>
                <p>Prix unitaire : €{item.pricePerUnit.toFixed(2)}</p>
                <p>Total : €{item.total.toFixed(2)}</p>
                <button onClick={() => removeItem(index)}>Retirer</button>
              </div>
            ))}
          </div>

          <div className="cart-total">
            <h3>Total général : €{getTotal()}</h3>
          </div>

          <button onClick={handleCheckout} className="checkout-button">
            Procéder au paiement
          </button>
        </>
      )}
    </div>
  );
}

export default CartPage;
