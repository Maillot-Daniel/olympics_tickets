import React, { useState } from 'react';
import { useCart } from '../../context/CartContext';
import { useNavigate } from 'react-router-dom';

function CartPage() {
  const { items, removeItem, clearCart } = useCart();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const totalPrice = items.reduce((acc, item) => acc + item.priceUnit * item.quantity, 0);

  const handleValidateOrder = async () => {
    const token = localStorage.getItem('olympics_auth_token');
    if (!token) {
      navigate('/login');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch("http://localhost:8080/api/cart/validate", {
        method: "POST",
        headers: {
          "Authorization": "Bearer " + token,
          "Content-Type": "application/json"
        }
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Erreur serveur lors de la validation: ${errorText}`);
      }

      alert("Commande validée !");
      clearCart();
      navigate('/public-events');
    } catch (error) {
      alert(error.message);
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleContinueShopping = () => {
    const token = localStorage.getItem('olympics_auth_token');
    if (!token) {
      navigate('/login');
    } else {
      navigate('/public-events');
    }
  };

  const handleClearCart = () => {
    if (window.confirm("Voulez-vous vraiment vider le panier ?")) {
      clearCart();
    }
  };

  if (items.length === 0) {
    return (
      <div>
        <h2>Votre panier est vide.</h2>
        <button onClick={handleContinueShopping}>
          Continuer mes achats
        </button>
      </div>
    );
  }

  return (
    <div>
      <h2>Votre panier</h2>
      <ul>
        {items.map((item, idx) => (
          <li key={item.id || idx} style={{ marginBottom: '1em' }}>
            <strong>{item.eventTitle}</strong> - {item.offerName} - {item.quantity} x {item.priceUnit.toFixed(2)} €
            <button 
              onClick={() => removeItem(item.eventId, item.offerTypeId)} 
              style={{ marginLeft: '1em', color: 'red' }}
              disabled={loading}
            >
              Supprimer
            </button>
          </li>
        ))}
      </ul>
      <p><strong>Total : {totalPrice.toFixed(2)} €</strong></p>
      <button onClick={handleValidateOrder} disabled={loading}>
        {loading ? 'Validation...' : 'Valider la commande'}
      </button>
      <button onClick={handleContinueShopping} style={{ marginLeft: '1em' }} disabled={loading}>
        Continuer mes achats
      </button>
      <button onClick={handleClearCart} style={{ marginLeft: '1em', color: 'orange' }} disabled={loading}>
        Vider le panier
      </button>
    </div>
  );
}

export default CartPage;
