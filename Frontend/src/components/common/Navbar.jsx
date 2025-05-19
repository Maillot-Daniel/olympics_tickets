import React, { useState, useEffect } from "react";
import { Link, useNavigate } from 'react-router-dom';
import UsersService from "../services/UsersService";
import OlympicLogo from "../../assets/images/logo-jo.png";
import "./Navbar.css";

function Navbar() {
  const [authState, setAuthState] = useState({
    isAuthenticated: UsersService.isAuthenticated(),
    isAdmin: UsersService.isAdmin()
  });
  const navigate = useNavigate();

  useEffect(() => {
    const checkAuth = () => {
      setAuthState({
        isAuthenticated: UsersService.isAuthenticated(),
        isAdmin: UsersService.isAdmin()
      });
    };

    const interval = setInterval(checkAuth, 1000); // Vérifie toutes les secondes
    return () => clearInterval(interval);
  }, []);

  const handleLogout = () => {
    if (window.confirm('Voulez-vous vraiment vous déconnecter ?')) {
      UsersService.logout();
      setAuthState({
        isAuthenticated: false,
        isAdmin: false
      });
      navigate('/login');
    }
  };

  return (
    <nav className="navbar">
       {/* Logo des Jeux Olympiques à droite */}
      <div className="olympic-logo-container">
        <img src={OlympicLogo} alt="Olympic Logo" className="olympic-logo" />
      </div>
      <div>
      <ul className="nav-list">

        {/* Liens à gauche */}
        <li className="nav-item">
          <Link to="/" className="nav-link">Accueil</Link>
        </li>

        <li className="nav-item">
          <Link to="/public-events" className="nav-link">Événements</Link>
        </li>

        {authState.isAuthenticated && (
          <>
            <li className="nav-item">
              <Link to="/profile" className="nav-link">Profil</Link>
            </li>
            <li className="nav-item">
              <Link to="/cart" className="nav-link">Panier</Link>
            </li>
          </>
        )}

        {authState.isAdmin && (
          <>
            <li className="nav-item">
              <Link to="/events" className="nav-link">Événements Admin</Link>
            </li>
            <li className="nav-item">
              <Link to="/admin/user-management" className="nav-link">Utilisateurs Admin</Link>
            </li>
            <li className="nav-item">
              <Link to="/create-event" className="nav-link">Créer un événement</Link>
            </li>
          </>
        )}

        {authState.isAuthenticated ? (
          <li className="nav-item">
            <button onClick={handleLogout} className="nav-link logout-btn">Déconnexion</button>
          </li>
        ) : (
          <li className="nav-item">
            <Link to="/login" className="nav-link">Connexion</Link>
          </li>
        )}
      </ul>
</div>
     
    </nav>
  );
}

export default Navbar;
