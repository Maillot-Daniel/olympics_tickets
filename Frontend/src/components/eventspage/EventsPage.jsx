// src/components/eventspage/EventsPage.jsx
/*import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import UsersService from '../services/UsersService';
import './EventsPage.css';

function EventsPage() {
  const [events, setEvents] = useState([]);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [ticketQuantity, setTicketQuantity] = useState(1);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEvents = async () => {
      try {
        // Redirige si l'utilisateur n'est pas connecté
        if (!UsersService.isAuthenticated()) {
          navigate('/login');
          return;
        }

        // Exemple de données simulées
        const mockEvents = [
          {
            id: 1,
            title: "Concert Rock",
            date: "2023-12-15",
            location: "Paris, Accor Arena",
            price: 45,
            description: "Un concert exceptionnel avec les plus grands artistes rock",
            remainingTickets: 150
          },
          {
            id: 2,
            title: "Festival Jazz",
            date: "2024-01-20",
            location: "Lyon, Halle Tony Garnier",
            price: 35,
            description: "3 jours de jazz avec des artistes internationaux",
            remainingTickets: 80
          },
          {
            id: 3,
            title: "Spectacle Comédie",
            date: "2024-02-10",
            location: "Marseille, Le Dôme",
            price: 25,
            description: "Les meilleurs humoristes français réunis pour une soirée",
            remainingTickets: 200
          }
        ];

        setEvents(mockEvents);
      } catch (error) {
        console.error("Erreur lors du chargement des événements :", error);
      }
    };

    fetchEvents();
  }, [navigate]);

  const handleSelectEvent = (event) => {
    setSelectedEvent(event);
    setTicketQuantity(1);
  };

  const handlePurchase = () => {
    alert(`Achat confirmé : ${ticketQuantity} billet(s) pour "${selectedEvent.title}"`);
    setSelectedEvent(null);
  };

  return (
    <div className="events-container">
      <h2>Événements Disponibles</h2>

      {!selectedEvent ? (
        <div className="events-list">
          {events.map(event => (
            <div key={event.id} className="event-card" onClick={() => handleSelectEvent(event)}>
              <h3>{event.title}</h3>
              <p className="event-date">{new Date(event.date).toLocaleDateString()}</p>
              <p className="event-location">{event.location}</p>
              <p className="event-price">{event.price}€</p>
              <p className="event-tickets">{event.remainingTickets} places restantes</p>
              <button className="select-btn">Sélectionner</button>
            </div>
          ))}
        </div>
      ) : (
        <div className="purchase-section">
          <h3>Vous avez sélectionné : {selectedEvent.title}</h3>
          <p className="event-details">{selectedEvent.description}</p>

          <div className="ticket-selection">
            <label>
              Nombre de billets :
              <input
                type="number"
                min="1"
                max={selectedEvent.remainingTickets}
                value={ticketQuantity}
                onChange={(e) =>
                  setTicketQuantity(
                    Math.max(1, Math.min(selectedEvent.remainingTickets, parseInt(e.target.value) || 1))
                  )
                }
              />
              <span> (Max: {selectedEvent.remainingTickets})</span>
            </label>

            <p className="total-price">
              Total : {selectedEvent.price * ticketQuantity}€
            </p>
          </div>

          <div className="action-buttons">
            <button onClick={() => setSelectedEvent(null)} className="cancel-btn">
              Annuler
            </button>
            <button onClick={handlePurchase} className="purchase-btn">
              Confirmer l'achat
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default EventsPage;*/
