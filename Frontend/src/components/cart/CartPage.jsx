import React, { useState } from 'react';
import { useCart } from '../../context/CartContext';
import { useNavigate } from 'react-router-dom';

function CartPage() {
  const { items, removeItem, clearCart } = useCart();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const totalPrice = items.reduce((acc, item) => acc + item.priceUnit * item.quantity, 0);

  const handleValidateOrder = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert("Veuillez vous connecter");
      navigate('/login');
      return;
    }
    setLoading(true);
    try {
     await fetch("http://localhost:8080/api/cart/validate", {
  method: "POST",
  headers: {
    "Authorization": "Bearer " + token,
    "Content-Type": "application/json"
  }
});
      alert("Commande validée !");
      clearCart();
    } catch (error) {
      alert("Erreur lors de la validation du panier");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleContinueShopping = () => {
    const token = localStorage.getItem('token');
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
