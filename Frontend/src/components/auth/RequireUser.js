// src/components/auth/RequireUser.js
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function RequireUser({ children }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) return null; // Ou un spinner

  return isAuthenticated ? children : <Navigate to="/login" />;
}
