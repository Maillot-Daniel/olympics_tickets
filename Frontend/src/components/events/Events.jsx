import React, { useEffect, useState } from 'react';
import axios from 'axios';

// Ajout des IDs pour correspondre aux offerTypeId attendus côté backend
const OFFERS = [
  { id: 1, name: 'Solo', people: 1, multiplier: 1 },
  { id: 2, name: 'Duo', people: 2, multiplier: 1.9 },
  { id: 3, name: 'Famille', people: 4, multiplier: 3.5 }
];

function Events() {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [selectedOffer, setSelectedOffer] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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

  const handleAddToCart = async () => {
    if (!selectedOffer || quantity < 1) {
      alert("Choisissez une offre et une quantité valide");
      return;
    }

    const offer = OFFERS.find(o => o.name === selectedOffer);
    if (!offer) {
      alert("Offre invalide");
      return;
    }

    if (!selectedEvent) {
      alert("Aucun événement sélectionné");
      return;
    }

    const token = localStorage.getItem('token');
    if (!token) {
      alert("Veuillez vous connecter");
      return;
    }

    // Construction de l'objet avec les clés camelCase attendues par le backend
    const itemToAdd = {
      eventId: selectedEvent.id,
      offerTypeId: offer.id,
      quantity: quantity
    };

    try {
      await axios.post('http://localhost:8080/api/cart/items', itemToAdd, {
        headers: { Authorization: `Bearer ${token}` }
      });
      alert("Ajouté au panier !");
      setSelectedEvent(null);
      setSelectedOffer('');
      setQuantity(1);
    } catch (err) {
      alert("Erreur lors de l'ajout au panier");
      console.error(err);
    }
  };

  if (loading) return <div>Chargement...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div>
      <h2>Liste des événements</h2>
      <ul>
        {events.map(event => (
          <li key={event.id}>
            <strong>{event.title}</strong> - {event.price} €
            <button onClick={() => setSelectedEvent(event)}>Acheter</button>
          </li>
        ))}
      </ul>

      {selectedEvent && (
        <div style={{ border: '1px solid black', padding: '1em', marginTop: '1em' }}>
          <h3>Achat pour : {selectedEvent.title}</h3>
          <label>
            Offre : 
            <select
              value={selectedOffer}
              onChange={e => setSelectedOffer(e.target.value)}
            >
              <option value="">-- Choisir une offre --</option>
              {OFFERS.map(offer => (
                <option key={offer.id} value={offer.name}>
                  {offer.name} ({offer.people} pers.) - {(selectedEvent.price * offer.multiplier).toFixed(2)} €
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
              value={quantity} 
              onChange={e => setQuantity(Math.max(1, Number(e.target.value)))}
            />
          </label>
          <br />
          <button onClick={handleAddToCart}>Ajouter au panier</button>
          <button onClick={() => setSelectedEvent(null)}>Annuler</button>
        </div>
      )}
    </div>
  );
}

export default Events;
