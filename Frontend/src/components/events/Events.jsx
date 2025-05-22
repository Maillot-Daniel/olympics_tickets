import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useCart } from '../../context/CartContext'; // Assure-toi du bon chemin
import { useNavigate } from 'react-router-dom';

// Offres avec IDs pour correspondance backend
const OFFERS = [
  { id: 1, name: 'Solo', people: 1, multiplier: 1 },
  { id: 2, name: 'Duo', people: 2, multiplier: 1.9 },
  { id: 3, name: 'Famille', people: 4, multiplier: 3.5 }
];

function Events() {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [selectedOfferId, setSelectedOfferId] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const { addItem } = useCart();
  const navigate = useNavigate();

  useEffect(() => {
    async function fetchEvents() {
      try {
        const res = await axios.get('http://localhost:8080/api/events');
        const data = Array.isArray(res.data.content) ? res.data.content : [];
        setEvents(data);
      } catch (err) {
        setError("Erreur lors du chargement des événements");
      } finally {
        setLoading(false);
      }
    }
    fetchEvents();
  }, []);

  const formatter = new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR',
  });

  const handleAddToCart = () => {
    if (!selectedOfferId) {
      alert("Choisissez une offre");
      return;
    }
    if (quantity < 1 || !Number.isInteger(quantity)) {
      alert("Veuillez saisir une quantité valide (entier >= 1)");
      return;
    }
    if (!selectedEvent) {
      alert("Aucun événement sélectionné");
      return;
    }

    const offer = OFFERS.find(o => o.id === parseInt(selectedOfferId));
    if (!offer) {
      alert("Offre invalide");
      return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      alert("Veuillez vous connecter");
      return;
    }

    // Ajout dans le panier via le contexte
    const itemToAdd = {
      eventId: selectedEvent.id,
      eventTitle: selectedEvent.title,
      offerTypeId: offer.id,
      offerName: offer.name,
      quantity: quantity,
      priceUnit: selectedEvent.price * offer.multiplier
    };

    addItem(itemToAdd);

    alert("Ajouté au panier !");
    setSelectedEvent(null);
    setSelectedOfferId('');
    setQuantity(1);

    // Redirection vers la page panier
    navigate('/cart');
  };

  if (loading) return <div>Chargement...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div>
      <h2>Liste des événements</h2>
      <ul>
        {events.map(event => (
          <li key={event.id} style={{ marginBottom: '1em' }}>
            <strong>{event.title}</strong> - {formatter.format(event.price)}
            <button style={{ marginLeft: '1em' }} onClick={() => setSelectedEvent(event)}>Acheter</button>
          </li>
        ))}
      </ul>

      {selectedEvent && (
        <div style={{ border: '1px solid black', padding: '1em', marginTop: '1em' }}>
          <h3>Achat pour : {selectedEvent.title}</h3>
          <label>
            Offre : 
            <select
              value={selectedOfferId}
              onChange={e => setSelectedOfferId(e.target.value)}
            >
              <option value="">-- Choisir une offre --</option>
              {OFFERS.map(offer => (
                <option key={offer.id} value={offer.id}>
                  {offer.name} ({offer.people} pers.) - {formatter.format(selectedEvent.price * offer.multiplier)}
                </option>
              ))}
            </select>
          </label>
          <br />
          <label>
            Quantité :
            <input 
              type="number" 
              min="1" 
              step="1"
              value={quantity} 
              onChange={e => {
                const val = Number(e.target.value);
                if (val >= 1) setQuantity(Math.floor(val));
              }}
            />
          </label>
          <br />
          <p>
            Prix total : {selectedOfferId 
              ? formatter.format(selectedEvent.price * OFFERS.find(o => o.id === parseInt(selectedOfferId)).multiplier * quantity) 
              : formatter.format(0)}
          </p>
          <button 
            onClick={handleAddToCart}
            disabled={!selectedOfferId || quantity < 1}
          >
            Ajouter au panier
          </button>
          <button onClick={() => setSelectedEvent(null)} style={{ marginLeft: '1em' }}>
            Annuler
          </button>
        </div>
      )}
    </div>
  );
}

export default Events;
