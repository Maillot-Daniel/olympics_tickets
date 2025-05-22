// src/services/UsersService.js
import axios from "axios";

class UsersService {
  static BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";
  static TOKEN_KEY = "olympics_auth_token";
  static ROLE_KEY = "olympics_user_role";
  static USER_ID_KEY = "olympics_user_id";

  static apiClient = axios.create({
    baseURL: UsersService.BASE_URL,
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
  });

  static init() {
    this.apiClient.interceptors.request.use(
      (config) => {
        const token = this.getToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    this.apiClient.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          this.clearAuth();
          window.dispatchEvent(new CustomEvent("authExpired"));
        }
        return Promise.reject(this.normalizeError(error));
      }
    );
  }

  static getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  static setAuthData(token, role, userId) {
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.ROLE_KEY, role);
    localStorage.setItem(this.USER_ID_KEY, userId || "");
    window.dispatchEvent(new CustomEvent("authChanged"));
  }

  static clearAuth() {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ROLE_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    window.dispatchEvent(new CustomEvent("authChanged"));
  }

  static logout() {
    this.clearAuth();
  }

  static isAuthenticated() {
    return !!this.getToken();
  }

  static isAdmin() {
    const role = localStorage.getItem(this.ROLE_KEY);
    return role && role.toLowerCase() === "admin";
  }

  static getCurrentUserId() {
    return localStorage.getItem(this.USER_ID_KEY);
  }

  static async login(email, password) {
  try {
    const response = await this.apiClient.post("/auth/login", { email, password });
    if (response.data.token) {
      this.setAuthData(response.data.token, response.data.role, response.data.userId);
    }
    return response.data;
  } catch (error) {
    throw this.normalizeError(error, "Échec de la connexion");
  }
}

  static async register(registrationData) {
    try {
      const response = await this.apiClient.post("/auth/register", registrationData);
      return response.data;
    } catch (error) {
      throw this.normalizeError(error, "Échec de l'inscription");
    }
  }

  static normalizeError(error, customMessage = "") {
    const normalizedError = new Error(customMessage || error.message);
    if (error.response) {
      normalizedError.status = error.response.status;
      normalizedError.data = error.response.data;
      normalizedError.message = error.response.data?.message || error.message;
    } else if (error.request) {
      normalizedError.status = 503;
      normalizedError.message = "Service indisponible";
    }
    return normalizedError;
  }
}

UsersService.init();

export default UsersService;
