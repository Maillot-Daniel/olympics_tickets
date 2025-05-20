import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { loadStripe } from '@stripe/stripe-js';

const stripePromise = loadStripe('pk_test_votreClePublique');

function CartPage() {
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);

  // Formatage sécurisé des prix
  const formatPrice = (value) =>
    typeof value === 'number' ? value.toFixed(2) : 'N/A';

  // Calcul du total général
  const getTotal = () =>
    cart.reduce(
      (sum, item) => sum + (typeof item.total_price === 'number' ? item.total_price : 0),
      0
    ).toFixed(2);

  // Récupération des données utilisateur et panier
  useEffect(() => {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');

    if (!token || !userId) {
      setLoading(false);
      return;
    }

    setUser({ token, userId });

    const fetchCart = async () => {
      try {
        const res = await axios.get(`http://localhost:8080/api/cart/active`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (res.data.status === 'ACTIVE') {
          setCart(res.data.items || []);
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

      if (response.status === 200) {
        setCart(cart.filter(item => item.id !== itemId));
      }
    } catch (error) {
      console.error("Erreur lors de la suppression", error);
    }
  };

  const handleCheckout = async () => {
    if (!user) {
      alert('Veuillez vous connecter avant de payer');
      return;
    }

    try {
      const stripe = await stripePromise;

      const checkoutData = {
        cartId: cart.find(item => item.cart_id)?.cart_id || null,
        userId: user.userId,
        items: cart.map(item => ({
          event_id: item.event_id,
          offer_type: item.offer_type,
          quantity: item.quantity,
          unit_price: item.unit_price,
        })),
      };

      const response = await axios.post(
        'http://localhost:8080/api/payment/create-checkout-session',
        checkoutData,
        { headers: { Authorization: `Bearer ${user.token}` } }
      );

      const { error } = await stripe.redirectToCheckout({
        sessionId: response.data.sessionId,
      });

      if (error) throw error;

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
                <h4>{item.event_title}</h4>
                <p>Type d'offre: {item.offer_type_name}</p>
                <p>Quantité: {item.quantity}</p>
                <p>Prix unitaire: €{formatPrice(item.unit_price)}</p>
                <p>Total: €{formatPrice(item.total_price)}</p>
                <button onClick={() => removeItem(item.id)}>Retirer</button>
              </div>
            ))}
          </div>

          <div className="cart-total">
            <h3>Total général: €{getTotal()}</h3>
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
