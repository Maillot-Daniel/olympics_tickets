import React, { useEffect, useState } from "react";
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../homepage/HomePage.css';

const HomePage = () => {
  const navigate = useNavigate();
  const [lastThreeEvents, setLastThreeEvents] = useState([]);

  useEffect(() => {
    fetchLastThreeEvents();
  }, []);

  const fetchLastThreeEvents = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/events');
      const events = response.data.content || response.data;
      const sorted = events.sort((a, b) => new Date(b.date) - new Date(a.date));
      setLastThreeEvents(sorted.slice(0, 3));
    } catch (error) {
      console.error("Erreur lors du chargement des événements récents:", error);
    }
  };

  const handleEventClick = (id) => {
    navigate(`/events?id=${id}`);
  };

  const handleReserveClick = () => navigate('/events');
  const handleLearnMore = () => navigate('/events');
  const handleAdventureReserve = () => navigate('/events');

  return (
    <div className="homepage">
      {/* Section Hero */}
      <section className="hero">
        <div className="hero-content">
          <h1>Bienvenue aux Jeux Olympiques France 2024</h1>
          <p className="hero-description">
            Dans l'univers des Jeux Olympiques, chaque événement est unique. 
            L'excellence et l'esprit de découverte se rencontrent pour créer 
            des expériences inoubliables.
          </p>

          <div className="hero-values">
            <span>UNITÉ ET PAIX</span>
            <span>EXCELLENCE SPORTIVE</span>
            <span>COMMUNAUTÉ MONDIALE</span>
          </div>

          <div className="hero-cta">
            <button className="cta-primary" onClick={handleReserveClick}>
              RÉSERVEZ VOS BILLETS →
            </button>
            <button className="cta-secondary" onClick={handleLearnMore}>
              EN SAVOIR PLUS
            </button>
          </div>
        </div>
      </section>

      {/* Section Épreuves phares */}
      <section className="featured-events">
        <h2>Les épreuves phares</h2>
        <p className="section-subtitle">Découvrez les moments les plus attendus des Jeux Olympiques 2024</p>

        <div className="events-grid">
          {lastThreeEvents.length > 0 ? (
            lastThreeEvents.map(event => (
              <div 
                key={event.id}
                className="event-card"
                onClick={() => handleEventClick(event.id)}
                style={{ cursor: "pointer" }}
              >
                <h3>{event.title}</h3>
                <p>{event.description}</p>
              </div>
            ))
          ) : (
            <p>Chargement des événements...</p>
          )}
        </div>
      </section>

      <hr className="separator" />

      {/* Section Rejoignez l'aventure */}
      <section className="join-adventure">
        <div className="adventure-content">
          <h2>Rejoignez l'aventure des Jeux Olympiques</h2>
          <p>Vivez l'excitation, soutenez vos athlètes favoris et faites partie de l'histoire.</p>

          <div className="highlights">
            <div className="highlight-item">
              <h4>Moments inoubliables</h4>
              <p>Athlètes en action</p>
            </div>
            <div className="highlight-item">
              <h4>Voltige historique</h4>
              <p>Découvertes époustouflantes</p>
            </div>
          </div>

          <div className="adventure-cta">
            <button className="cta-primary" onClick={handleAdventureReserve}>
              Réservez vos billets
            </button>
            <button className="cta-secondary" onClick={handleLearnMore}>
              En savoir plus
            </button>
          </div>
        </div>
      </section>

      {/* Section Billetterie */}
      <section className="ticketing">
        <h2>BILLETTERIE</h2>
        <p className="section-subtitle">Comment réserver vos places</p>
        <p className="ticketing-description">
          Suivez ces étapes simples pour garantir votre présence aux Jeux Olympiques 2024.
        </p>

        <div className="steps">
          <div className="step">
            <div className="step-number">1</div>
            <h3>Créer un compte</h3>
            <p>Inscrivez-vous en quelques clics pour accéder à la billetterie.</p>
          </div>
          <div className="step">
            <div className="step-number">2</div>
            <h3>Choisir vos événements</h3>
            <p>Sélectionnez les épreuves que vous souhaitez voir.</p>
          </div>
          <div className="step">
            <div className="step-number">3</div>
            <h3>Confirmer votre réservation</h3>
            <p>Finalisez votre achat et préparez-vous à vivre un moment unique.</p>
          </div>
        </div>
      </section>
    </div>
  );
};

export default HomePage;
