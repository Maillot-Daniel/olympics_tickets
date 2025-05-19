import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import UsersService from "../services/UsersService";
import "./LoginPage.css";

function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const userData = await UsersService.login(email, password);
      
      if (userData.token) {
        localStorage.setItem("token", userData.token);
        localStorage.setItem("role", userData.role);
        navigate("/profile");
      } else {
        setError(userData.error || "Authentication failed");
      }
    } catch (error) {
      console.error("Login error:", error);
      setError(error.response?.data?.message || error.message || "Login failed");
    } finally {
      setIsLoading(false);
    }
  };

   return (
    <div className="page-wrapper">
      {/* Bloc de connexion */}
      <div className="auth-container">
        <h2>Login</h2>
        {error && <p className="error-message">{error}</p>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email: </label>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Password: </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
            />
          </div>
          <button type="submit" disabled={isLoading}>
            {isLoading ? "Logging in..." : "Login"}
          </button>
        </form>
      </div>

      {/* Bloc séparé de présentation */}
      <div className="presentation-box">
        <h3>Bienvenue aux Jeux Olympiques 2024</h3>
        <img src="/img_jeux_olympic.webp" alt="Jeux Olympiques 2024" className="olympic-image" />
        <p>
          Plongez dans l'univers des Jeux Olympiques de Paris 2024 ! 
          Célébrez le sport, la passion et l’unité à travers des compétitions 
          palpitantes dans des lieux emblématiques de la capitale.
        </p>
        <p>
          Achetez vos billets, suivez les épreuves et vibrez avec le monde entier !
        </p>
      </div>
    </div>
  );
}

export default LoginPage;