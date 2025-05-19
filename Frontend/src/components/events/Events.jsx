import React, { useEffect, useState } from 'react';
import axios from 'axios';
import '../events/Events.css';
import { useCart } from '../../context/CartContext';

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
  const [error, setError] = useState(null);

  const { cart, addToCart, isEmpty } = useCart();

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/events');
        const eventsData = Array.isArray(response.data.content) ? response.data.content : [];
        setEvents(eventsData);
      } catch (err) {
        setError(err.message || 'Erreur inconnue');
      }
    };
    fetchEvents();
  }, []);

  const handleBuyClick = (event) => {
    setSelectedEvent(event);
    setSelectedOffer('');
    setQuantity(1);
  };

  const handleAddToCart = async () => {
    if (!selectedOffer || quantity < 1) return;

    const offerDetails = OFFERS.find(o => o.name === selectedOffer);
    if (!offerDetails) return;

    const placesToRemove = quantity * offerDetails.people;

    if (selectedEvent.remainingTickets < placesToRemove) {
      alert("Pas assez de places disponibles pour cette offre et quantité.");
      return;
    }

    try {
      await addToCart(selectedEvent.id, selectedOffer, quantity);
      setEvents(events.map(ev => {
        if (ev.id === selectedEvent.id) {
          return {
            ...ev,
            remainingTickets: ev.remainingTickets - placesToRemove
          };
        }
        return ev;
      }));
      setSelectedEvent(null);
    } catch (err) {
      console.error("Erreur lors de l'ajout au panier :", err);
      alert('Échec de l’ajout au panier.');
    }
  };

  const handleCheckout = async () => {
    if (isEmpty) return;

    try {
      const token = localStorage.getItem('token');
      const response = await axios.post('http://localhost:8080/api/payment/create-checkout-session', {
        cartItems: cart.items,
      }, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });

      window.location.href = response.data.sessionUrl;
    } catch (err) {
      console.error('Erreur lors du paiement:', err);
      alert('Une erreur est survenue lors du paiement');
    }
  };

  const maxQuantity = selectedEvent && selectedOffer
    ? Math.floor(selectedEvent.remainingTickets / OFFERS.find(o => o.name === selectedOffer).people)
    : 1;

  if (error) return <div>Erreur: {error}</div>;

  return (
    <div className="events-container">
      <h2>Liste des Événements</h2>

      {/* Panier */}
      <div className="cart-section">
        <h3>Votre Panier ({cart.items.length} article{cart.items.length > 1 ? 's' : ''})</h3>
        {cart.items.length > 0 ? (
          <>
            <ul className="cart-items">
              {cart.items.map((item, index) => (
                <li key={index} className="cart-item">
                  <span>{item.quantity}x {item.eventTitle} ({item.offerType})</span>
                  <span>{item.totalPrice.toFixed(2)} €</span>
                </li>
              ))}
            </ul>
            <div className="cart-total">
              Total: {cart.items.reduce((sum, item) => sum + item.totalPrice, 0).toFixed(2)} €
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

      {/* Événements */}
      <div className="event-grid">
        {events.map(event => (
          <div className="event-card" key={event.id}>
            <h3 className="event-title">{event.title}</h3>
            <p className="event-description">{event.description}</p>
            <div className="event-details">
              <p><strong>Date:</strong> {new Date(event.date).toLocaleDateString()}</p>
              <p><strong>Lieu:</strong> {event.location}</p>
              <p><strong>Prix:</strong> {event.price.toFixed(2)} €</p>
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

      {/* Modal */}
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
                max={maxQuantity}
                value={quantity}
                onChange={e => {
                  let val = Math.max(1, Number(e.target.value));
                  if (val > maxQuantity) val = maxQuantity;
                  setQuantity(val);
                }}
                className="quantity-input"
              />
              <small>Max {maxQuantity} {maxQuantity > 1 ? 'offres' : 'offre'} disponibles</small>
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
                disabled={!selectedOffer || quantity < 1 || quantity > maxQuantity}
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
