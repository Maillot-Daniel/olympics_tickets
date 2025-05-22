import React, { createContext, useContext, useState, useEffect } from "react";
import UsersService from "../components/services/UsersService";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState({
    id: localStorage.getItem("olympics_user_id") || null,
    role: localStorage.getItem("olympics_user_role") || null,
  });
  const [isAuthenticated, setIsAuthenticated] = useState(!!UsersService.getToken());

  // Fonction de connexion
  const login = ({ token, id, role }) => {
    localStorage.setItem("olympics_auth_token", token);
    localStorage.setItem("olympics_user_id", id);
    localStorage.setItem("olympics_user_role", role);

    setUser({ id, role });
    setIsAuthenticated(true);

    // Ajouter token à axios pour requêtes futures
    UsersService.apiClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  };

  // Fonction de déconnexion
  const logout = () => {
    localStorage.removeItem("olympics_auth_token");
    localStorage.removeItem("olympics_user_id");
    localStorage.removeItem("olympics_user_role");

    setUser({ id: null, role: null });
    setIsAuthenticated(false);

    delete UsersService.apiClient.defaults.headers.common["Authorization"];
  };

  // En cas de reload, remettre le token dans axios si présent
  useEffect(() => {
    const token = UsersService.getToken();
    if (token) {
      UsersService.apiClient.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
