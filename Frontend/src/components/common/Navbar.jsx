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
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const checkAuth = () => {
      setAuthState({
        isAuthenticated: UsersService.isAuthenticated(),
        isAdmin: UsersService.isAdmin()
      });
    };

    const interval = setInterval(checkAuth, 1000);
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

  const toggleMobileMenu = () => {
    setIsMobileMenuOpen(!isMobileMenuOpen);
  };

  return (
    <>
      <nav className="navbar">
        <div className="olympic-logo-container">
          <img src={OlympicLogo} alt="Olympic Logo" className="olympic-logo" />
        </div>
        
        {/* Bouton menu mobile */}
        <button 
          className="mobile-menu-button"
          onClick={toggleMobileMenu}
          aria-label="Toggle menu"
        >
          ☰
        </button>

        <div className={`nav-container ${isMobileMenuOpen ? 'open' : ''}`}>
          <ul className="nav-list">
            <li className="nav-item">
              <Link to="/home" className="nav-link" onClick={() => setIsMobileMenuOpen(false)}>Accueil</Link>
            </li>

            <li className="nav-item">
              <Link to="/public-events" className="nav-link" onClick={() => setIsMobileMenuOpen(false)}>Événements</Link>
            </li>

            {authState.isAuthenticated && (
              <>
                <li className="nav-item">
                  <Link to="/profile" className="nav-link" onClick={() => setIsMobileMenuOpen(false)}>Profil</Link>
                </li>
                <li className="nav-item">
                  <Link to="/cart" className="nav-link" onClick={() => setIsMobileMenuOpen(false)}>Panier</Link>
                </li>
              </>
            )}

            {authState.isAdmin && (
              <>
                <li className="nav-item">
                  <Link to="/events" className="nav-link" onClick={() => setIsMobileMenuOpen(false)}>Événements Admin</Link>
                </li>
                <li className="nav-item">
                  <Link to="/admin/user-management" className="nav-link" onClick={() => setIsMobileMenuOpen(false)}>Utilisateurs Admin</Link>
                </li>
                <li className="nav-item">
                  <Link to="/create-event" className="nav-link" onClick={() => setIsMobileMenuOpen(false)}>Créer un événement</Link>
                </li>
              </>
            )}

            {authState.isAuthenticated ? (
              <li className="nav-item">
                <button onClick={handleLogout} className="nav-link logout-btn">Déconnexion</button>
              </li>
            ) : (
              <li className="nav-item">
                <Link to="/login" className="nav-link" onClick={() => setIsMobileMenuOpen(false)}>Connexion</Link>
              </li>
            )}
          </ul>
        </div>
      </nav>
      
      {/* Espace réservé pour éviter que le contenu soit caché */}
      <div className="navbar-spacer"></div>
    </>
  );
}

export default Navbar;