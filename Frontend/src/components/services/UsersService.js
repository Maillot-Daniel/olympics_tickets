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
        console.log("[UsersService] Interceptor Request: Token =", token);
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        console.error("[UsersService] Interceptor Request Error:", error);
        return Promise.reject(error);
      }
    );

    this.apiClient.interceptors.response.use(
      (response) => {
        console.log("[UsersService] Interceptor Response: status =", response.status);
        return response;
      },
      (error) => {
        console.error("[UsersService] Interceptor Response Error:", error.response?.status);
        if (error.response?.status === 401) {
          console.warn("[UsersService] Auth expired, clearing auth data");
          this.clearAuth();
          window.dispatchEvent(new CustomEvent("authExpired"));
        }
        return Promise.reject(this.normalizeError(error));
      }
    );

    console.log("[UsersService] Initialized with baseURL:", this.BASE_URL);
  }

  static getToken() {
    const token = localStorage.getItem(this.TOKEN_KEY);
    console.log("[UsersService] getToken:", token);
    return token;
  }

  static setAuthData(token, role, userId) {
    console.log("[UsersService] setAuthData:", { token, role, userId });
    localStorage.setItem(this.TOKEN_KEY, token);
    localStorage.setItem(this.ROLE_KEY, role);
    localStorage.setItem(this.USER_ID_KEY, userId || "");
    window.dispatchEvent(new CustomEvent("authChanged"));
  }

  static clearAuth() {
    console.log("[UsersService] clearAuth called");
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ROLE_KEY);
    localStorage.removeItem(this.USER_ID_KEY);
    window.dispatchEvent(new CustomEvent("authChanged"));
  }

  static isAuthenticated() {
    const isAuth = !!this.getToken();
    console.log("[UsersService] isAuthenticated:", isAuth);
    return isAuth;
  }

  static isAdmin() {
    const role = localStorage.getItem(this.ROLE_KEY);
    const isAdmin = role && role.toLowerCase() === "admin";
    console.log("[UsersService] isAdmin:", isAdmin, "(role:", role, ")");
    return isAdmin;
  }

  static getCurrentUserId() {
    const userId = localStorage.getItem(this.USER_ID_KEY);
    console.log("[UsersService] getCurrentUserId:", userId);
    return userId;
  }

  // Authentification
  static async login(email, password) {
    console.log("[UsersService] login called with email:", email);
    try {
      const response = await this.apiClient.post("/auth/login", {
        email,
        password
      });
      return response.data;
    } catch (error) {
      console.error("[UsersService] login error:", error);
      throw this.normalizeError(error, "Échec de la connexion");
    }
  }

  // Inscription
  static async register(registrationData) {
    console.log("[UsersService] register called");
    try {
      const response = await this.apiClient.post("/auth/register", registrationData);
      console.log("[UsersService] register response:", response.data);
      return response.data;
    } catch (error) {
      console.error("[UsersService] register error:", error);
      throw this.normalizeError(error, "Échec de l'inscription");
    }
  }

  // Récupérer le profil utilisateur connecté
  static async getProfile() {
    console.log("[UsersService] getProfile called");
    try {
      const response = await this.apiClient.get("/adminuser/get-profile");
      console.log("[UsersService] getProfile response:", response.data);
      return response.data;
    } catch (error) {
      console.error("[UsersService] getProfile error:", error);
      throw this.normalizeError(error, "Échec de la récupération du profil");
    }
  }

  static async getAllUsers() {
    console.log("[UsersService] getAllUsers called");
    try {
      const response = await this.apiClient.get("/admin/get-all-users");
      console.log("[UsersService] getAllUsers response:", response.data);
      return response.data;
    } catch (error) {
      console.error("[UsersService] getAllUsers error:", error);
      throw this.normalizeError(error, "Erreur lors de la récupération des utilisateurs");
    }
  }

  static async getUserById(userId) {
    console.log("[UsersService] getUserById called with userId:", userId);
    try {
      const response = await this.apiClient.get(`/admin/get-users/${userId}`);
      console.log("[UsersService] getUserById response:", response.data);
      return response.data;
    } catch (error) {
      console.error("[UsersService] getUserById error:", error);
      throw this.normalizeError(error, "Erreur lors de la récupération de l'utilisateur");
    }
  }

  static async deleteUser(userId) {
    console.log("[UsersService] deleteUser called with userId:", userId);
    try {
      const response = await this.apiClient.delete(`/admin/delete/${userId}`);
      console.log("[UsersService] deleteUser response:", response.data);
      return response.data;
    } catch (error) {
      console.error("[UsersService] deleteUser error:", error);
      throw this.normalizeError(error, "Erreur lors de la suppression de l'utilisateur");
    }
  }

  static async updateUser(userId, userData) {
    console.log("[UsersService] updateUser called with userId:", userId, "userData:", userData);
    try {
      const response = await this.apiClient.put(`/admin/update/${userId}`, userData);
      console.log("[UsersService] updateUser response:", response.data);
      return response.data;
    } catch (error) {
      console.error("[UsersService] updateUser error:", error);
      throw this.normalizeError(error, "Erreur lors de la mise à jour de l'utilisateur");
    }
  }

  // Mot de passe oublié - envoyer mail réinitialisation
  static async requestPasswordReset(email) {
    console.log("[UsersService] requestPasswordReset called with email:", email);
    try {
      const response = await this.apiClient.post("/auth/password-reset-request", { email });
      console.log("[UsersService] requestPasswordReset response:", response.data);
      return response.data;
    } catch (error) {
      console.error("[UsersService] requestPasswordReset error:", error);
      throw this.normalizeError(error, "Erreur lors de la demande de réinitialisation");
    }
  }

  // Normalisation des erreurs pour UI
  static normalizeError(error, customMessage = "") {
    console.log("[UsersService] normalizeError called:", customMessage || error.message);
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