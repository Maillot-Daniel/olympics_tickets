import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../events/Events.css';

const OFFERS = [
  { name: 'Solo', people: 1, multiplier: 1 },
  { name: 'Duo', people: 2, multiplier: 1.9 },
  { name: 'Famille', people: 4, multiplier: 3.5 }
];

function Events() {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [selectedOffer, setSelectedOffer] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [cart, setCart] = useState(() => {
    const savedCart = localStorage.getItem('cart');
    return savedCart ? JSON.parse(savedCart) : [];
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/events');
        console.log('Données reçues:', response.data);
        const eventsData = Array.isArray(response.data.content) ? response.data.content : [];
        setEvents(eventsData);
      } catch (err) {
        setError(err.message || 'Erreur inconnue');
      } finally {
        setLoading(false);
      }
    };
    fetchEvents();
  }, []);

  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(cart));
  }, [cart]);

  const handleBuyClick = (event) => {
    setSelectedEvent(event);
    setSelectedOffer('');
    setQuantity(1);
  };

  const handleAddToCart = () => {
    if (!selectedOffer || quantity < 1) return;

    const offerDetails = OFFERS.find(o => o.name === selectedOffer);
    if (!offerDetails) return;

    const total = selectedEvent.price * offerDetails.multiplier * quantity;

    const item = {
      eventId: selectedEvent.id,
      title: selectedEvent.title,
      offer: selectedOffer,
      people: offerDetails.people,
      pricePerUnit: selectedEvent.price * offerDetails.multiplier,
      quantity,
      total
    };

    setCart([...cart, item]);
    setSelectedEvent(null);
  };

  const removeFromCart = (index) => {
    const newCart = [...cart];
    newCart.splice(index, 1);
    setCart(newCart);
  };

  const handleCheckout = async () => {
    if (cart.length === 0) return;

    try {
      const response = await axios.post('http://localhost:8080/api/payment/create-checkout-session', {
        cartItems: cart,
      });

      // Redirection vers Stripe
      window.location.href = response.data.sessionUrl;
    } catch (err) {
      console.error('Erreur lors du paiement:', err);
      alert('Une erreur est survenue lors du paiement');
    }
  };

  if (loading) return <div>Chargement en cours...</div>;
  if (error) return <div>Erreur: {error}</div>;

  return (
    <div className="events-container">
      <h2>Liste des Événements</h2>
      
      {/* Section Panier */}
      <div className="cart-section">
        <h3>Votre Panier ({cart.length} articles)</h3>
        {cart.length > 0 ? (
          <>
            <ul className="cart-items">
              {cart.map((item, index) => (
                <li key={index} className="cart-item">
                  <span>{item.quantity}x {item.title} ({item.offer})</span>
                  <span>{item.total.toFixed(2)} €</span>
                  <button 
                    onClick={() => removeFromCart(index)}
                    className="remove-btn"
                  >
                    ×
                  </button>
                </li>
              ))}
            </ul>
            <div className="cart-total">
              Total: {cart.reduce((sum, item) => sum + item.total, 0).toFixed(2)} €
            </div>
            <button 
              onClick={handleCheckout}
              className="checkout-btn"
            >
              Payer avec Stripe
            </button>
          </>
        ) : (
          <p className="empty-cart">Votre panier est vide</p>
        )}
      </div>

      {/* Liste des événements */}
      <div className="event-grid">
        {events.map(event => (
          <div className="event-card" key={event.id}>
            <h3 className="event-title">{event.title}</h3>
            <p className="event-description">{event.description}</p>
            <div className="event-details">
              <p><strong>Date:</strong> {new Date(event.date).toLocaleDateString()}</p>
              <p><strong>Lieu:</strong> {event.location}</p>
              <p><strong>Prix:</strong> {event.price} €</p>
              <p><strong>Places restantes:</strong> {event.remainingTickets}</p>
            </div>
            <button 
              onClick={() => handleBuyClick(event)}
              disabled={event.remainingTickets <= 0}
              className="buy-btn"
            >
              {event.remainingTickets > 0 ? 'Acheter' : 'Complet'}
            </button>
          </div>
        ))}
      </div>

      {/* Modal de sélection */}
      {selectedEvent && (
        <div className="modal-overlay">
          <div className="purchase-modal">
            <button className="close-modal" onClick={() => setSelectedEvent(null)}>×</button>
            <h3>Choisir une offre pour {selectedEvent.title}</h3>
            
            <div className="form-group">
              <label>Offre :</label>
              <select 
                value={selectedOffer} 
                onChange={e => setSelectedOffer(e.target.value)}
                className="offer-select"
              >
                <option value="">-- Choisir une offre --</option>
                {OFFERS.map(offer => (
                  <option key={offer.name} value={offer.name}>
                    {offer.name} ({offer.people} pers.) - {(selectedEvent.price * offer.multiplier).toFixed(2)} €
                  </option>
                ))}
              </select>
            </div>
            
            <div className="form-group">
              <label>Quantité :</label>
              <input
                type="number"
                min="1"
                max={selectedEvent.remainingTickets}
                value={quantity}
                onChange={e => setQuantity(Math.max(1, Number(e.target.value)))}
                className="quantity-input"
              />
            </div>
            
            {selectedOffer && (
              <div className="total-section">
                <strong>Total :</strong> {(
                  selectedEvent.price *
                  OFFERS.find(o => o.name === selectedOffer).multiplier *
                  quantity
                ).toFixed(2)} €
              </div>
            )}
            
            <div className="modal-actions">
              <button 
                onClick={handleAddToCart}
                disabled={!selectedOffer || quantity < 1}
                className="add-to-cart-btn"
              >
                Ajouter au panier
              </button>
              <button 
                onClick={() => setSelectedEvent(null)}
                className="cancel-btn"
              >
                Annuler
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Events;
