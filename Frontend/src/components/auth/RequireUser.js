// src/components/auth/RequireUser.js
import React from 'react';
import { Navigate } from 'react-router-dom';
import UsersService from '../services/UsersService';  // Assurez-vous que ce fichier existe et que la méthode isAuthenticated() y est définie

function RequireUser({ children }) {
  // Vérifier si l'utilisateur est authentifié
  if (!UsersService.isAuthenticated()) {
    // Si non, rediriger vers la page de login
    return <Navigate to="/login" />;
  }

  // Si l'utilisateur est authentifié, afficher les enfants (la page demandée)
  return children;
}

export default RequireUser;
