import React, { useEffect, useState } from 'react';
import axios from 'axios';
import './EventList.css';

function EventList() {
  const [events, setEvents] = useState([]);
  const [editEvent, setEditEvent] = useState(null);
  const [editFormData, setEditFormData] = useState({
    title: '',
    description: '',
    date: '',
    location: '',
    price: 0,
    totalTickets: 0
  });

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = () => {
    axios.get('http://localhost:8080/api/events')
      .then(res => {
        setEvents(res.data.content || res.data);
      })
      .catch(err => console.error('Error fetching events:', err));
  };

  const handleDelete = async (id) => {
    const token = localStorage.getItem('token');
    if (!token) {
      alert('Vous devez être connecté pour supprimer un événement.');
      return;
    }

    try {
      await axios.delete(`http://localhost:8080/api/events/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setEvents(events.filter(event => event.id !== id));
      alert('Événement supprimé avec succès');
    } catch (error) {
      console.error('Error deleting event:', error);
      alert('Erreur lors de la suppression');
    }
  };

  const handleEditClick = (event) => {
    setEditEvent(event);
    
    // Formatage de la date pour l'input
    const eventDate = new Date(event.date);
    const formattedDate = eventDate.toISOString().slice(0, 16); // Format "YYYY-MM-DDTHH:mm"
    
    setEditFormData({
      title: event.title,
      description: event.description,
      date: formattedDate,
      location: event.location,
      price: event.price,
      totalTickets: event.totalTickets
    });
  };

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditFormData({
      ...editFormData,
      [name]: name === 'price' ? parseFloat(value) :
              name === 'remainingTickets' ? parseInt(value, 10) :
              value
    });
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('token');
    
    if (!token) {
      alert('Vous devez être connecté pour modifier un événement.');
      return;
    }

    try {
      // Formatage des données avant envoi
      const dataToSend = {
        ...editFormData,
        date: editFormData.date // La date est déjà au bon format
      };

      const response = await axios.put(
        `http://localhost:8080/api/events/${editEvent.id}`,
        dataToSend,
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      
      setEvents(events.map(event => 
        event.id === editEvent.id ? response.data : event
      ));
      setEditEvent(null);
      alert('Événement mis à jour avec succès');
    } catch (error) {
      console.error('Error updating event:', error.response || error);
      if (error.response?.status === 403) {
        alert('Vous n\'avez pas la permission de modifier cet événement.');
      } else {
        alert(error.response?.data?.message || 'Erreur lors de la mise à jour');
      }
    }
  };

  return (
    <div className="event-list-container">
      <h2>Liste des Événements</h2>

      {/* Formulaire de modification */}
      {editEvent && (
        <div className="edit-form-overlay">
          <div className="edit-form-container">
            <h3>Modifier l'événement</h3>
            <form onSubmit={handleEditSubmit}>
              <div className="form-group">
                <label>Titre:</label>
                <input
                  type="text"
                  name="title"
                  value={editFormData.title}
                  onChange={handleEditChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Description:</label>
                <textarea
                  name="description"
                  value={editFormData.description}
                  onChange={handleEditChange}
                  required
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Date et Heure:</label>
                  <input
                    type="datetime-local"
                    name="date"
                    value={editFormData.date}
                    onChange={handleEditChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Lieu:</label>
                  <input
                    type="text"
                    name="location"
                    value={editFormData.location}
                    onChange={handleEditChange}
                    required
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Prix (€):</label>
                  <input
                    type="number"
                    name="price"
                    min="0"
                    step="0.01"
                    value={editFormData.price}
                    onChange={handleEditChange}
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Places restantes:</label>
                  <input
                    type="number"
                    name="totalTickets"
                    min="0"
                    value={editFormData.totalTickets}
                    onChange={handleEditChange}
                    required
                  />
                </div>
              </div>

              <div className="form-actions">
                <button type="submit" className="save-btn">Enregistrer</button>
                <button
                  type="button"
                  className="cancel-btn"
                  onClick={() => setEditEvent(null)}
                >
                  Annuler
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Liste des événements */}
      <div className="events-grid">
        {events.length > 0 ? (
          events.map(event => (
            <div className="event-card" key={event.id}>
              <div className="card-header">
                <h3>{event.title}</h3>
                <p className="event-date">
                  {new Date(event.date).toLocaleString('fr-FR', {
                    weekday: 'long',
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit'
                  })}
                </p>
              </div>

              <div className="card-body">
                <p className="event-description">{event.description}</p>
                <div className="event-details">
                  <p><span className="detail-label">Lieu:</span> {event.location}</p>
                  <p><span className="detail-label">Prix:</span> {event.price.toFixed(2)} €</p>
                  <p className={event.totalTickets < 10 ? 'low-tickets' : ''}>
                    <span className="detail-label">Places:</span> {event.totalTickets}
                  </p>
                </div>
              </div>

              <div className="card-actions">
                <button
                  className="edit-btn"
                  onClick={() => handleEditClick(event)}
                >
                  Modifier
                </button>
                <button
                  className="delete-btn"
                  onClick={() => {
                    if (window.confirm('Êtes-vous sûr de vouloir supprimer cet événement ?')) {
                      handleDelete(event.id);
                    }
                  }}
                >
                  Supprimer
                </button>
              </div>
            </div>
          ))
        ) : (
          <p>Aucun événement trouvé.</p>
        )}
      </div>
    </div>
  );
}

export default EventList;