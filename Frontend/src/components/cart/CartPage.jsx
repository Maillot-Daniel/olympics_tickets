import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { loadStripe } from '@stripe/stripe-js';
import './CartPage.css';

const stripePromise = loadStripe('pk_test_votreClePublique'); // remplace par ta vraie clé

function CartPage() {
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);

  const formatPrice = (value) =>
    typeof value === 'number' ? value.toFixed(2) : 'N/A';

  const getTotal = () =>
    cart.reduce((sum, item) => sum + (item.totalPrice ? Number(item.totalPrice) : 0), 0).toFixed(2);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    if (!token || !userId) {
      console.warn("Token ou userId manquant");
      setLoading(false);
      return;
    }

    setUser({ token, userId });

    const fetchCart = async () => {
      try {
        const res = await axios.get('http://localhost:8080/api/cart/active', {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (res.status === 200 && res.data.status === 'ACTIVE') {
          setCart(res.data.items || []);
        } else {
          console.warn("Panier inactif ou vide", res.data);
        }
      } catch (err) {
        console.error("Erreur lors du chargement du panier", err);
      } finally {
        setLoading(false);
      }
    };

    fetchCart();
  }, []);

  const removeItem = async (itemId) => {
    try {
      const response = await axios.delete(
        `http://localhost:8080/api/cart/items/${itemId}`,
        { headers: { Authorization: `Bearer ${user.token}` } }
      );

      if (response.status === 204) {
        setCart(cart.filter(item => item.id !== itemId));
      }
    } catch (error) {
      console.error("Erreur lors de la suppression de l'article", error);
    }
  };

  const clearCart = async () => {
    if (!window.confirm("Voulez-vous vraiment vider le panier ?")) return;

    const itemIds = cart.map(item => item.id);

    for (let itemId of itemIds) {
      await removeItem(itemId);
    }
  };

  const handleCheckout = async () => {
    if (!user) {
      alert('Veuillez vous connecter avant de payer');
      return;
    }

    if (!window.confirm("Confirmez-vous le paiement de votre commande ?")) return;

    const cartId = cart.length > 0 ? cart[0].cartId : null;

    if (!cartId) {
      alert("Impossible d'identifier le panier.");
      return;
    }

    try {
      const stripe = await stripePromise;

      const checkoutData = {
        cartId,
        userId: user.userId,
        cartItems: cart.map(item => ({
          eventId: item.eventId,
          offerTypeName: item.offerTypeName,
          quantity: item.quantity,
          unitPrice: item.unitPrice,
          totalPrice: item.totalPrice,
        })),
      };

      const response = await axios.post(
        'http://localhost:8080/api/payment/create-checkout-session',
        checkoutData,
        { headers: { Authorization: `Bearer ${user.token}` } }
      );

      const sessionId = response.data.sessionId;

      if (!sessionId) throw new Error("Aucune sessionId retournée par le backend");

      const { error } = await stripe.redirectToCheckout({ sessionId });

      if (error) {
        console.error("Stripe checkout error", error);
        alert("Erreur lors de la redirection vers Stripe");
      }

    } catch (error) {
      console.error("Erreur de paiement:", error.response?.data || error.message);
      alert(error.response?.data?.message || 'Erreur lors du paiement');
    }
  };

  if (loading) return <div>Chargement du panier...</div>;
  if (!user) return <div>Veuillez vous connecter pour accéder à votre panier</div>;

  return (
    <div className="cart-container">
      <h2>Votre Panier</h2>

      {cart.length === 0 ? (
        <p>Votre panier est vide</p>
      ) : (
        <>
          <div className="cart-items">
            {cart.map((item) => (
              <div key={item.id} className="cart-item">
                <h4>{item.eventTitle}</h4>
                <p>Type d'offre : {item.offerTypeName}</p>
                <p>Quantité : {item.quantity}</p>
                <p>Prix unitaire : €{formatPrice(item.unitPrice)}</p>
                <p>Total : €{formatPrice(item.totalPrice)}</p>
                <button onClick={() => removeItem(item.id)}>Retirer</button>
              </div>
            ))}
          </div>

          <div className="cart-total">
            <h3>Total général : €{getTotal()}</h3>
          </div>

          <div className="cart-actions">
            <button onClick={handleCheckout} className="checkout-button">
              Procéder au paiement
            </button>
            <button onClick={clearCart} className="clear-button">
              Vider le panier
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default CartPage;
