import axios from "axios";

class UsersService {
  static BASE_URL = "http://localhost:8080";

  static apiClient = axios.create({
    baseURL: this.BASE_URL,
    timeout: 10000,
    headers: {
      "Content-Type": "application/json",
    },
  });

  // Flag pour empêcher d'ajouter plusieurs intercepteurs
  static isInterceptorInitialized = false;

  /** Initialise l'intercepteur pour ajouter le token dans les headers */
  static init() {
  if (this.isInterceptorInitialized) {
    console.log("Intercepteur déjà initialisé."); // 🟡
    return;
  }

  console.log("Initialisation de l'intercepteur Axios..."); // 🟢

  this.apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    console.log("Token récupéré du localStorage :", token); // 🟢

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
      console.log("Header Authorization ajouté :", config.headers.Authorization); // 🟢
    } else {
      console.warn("⚠️ Aucun token trouvé dans le localStorage."); // ⚠️
    }

    return config;
  });

  this.isInterceptorInitialized = true;
  console.log("Intercepteur Axios initialisé ✅"); // ✅
}

  static async login(email, password) {
    try {
      const response = await this.apiClient.post("/auth/login", { email, password });
      if (response.data.token) {
        localStorage.setItem("token", response.data.token);
        localStorage.setItem("role", response.data.role);
        localStorage.setItem("userId", response.data.userId || "");
        this.init(); // Assure l'intercepteur est prêt après login
        console.log("Rôle utilisateur connecté:", localStorage.getItem("role"));
      }
      return response.data;
    } catch (error) {
      this.handleError(error);
      throw error;
    }
  }

  static async register(userData) {
    try {
      const response = await this.apiClient.post("/auth/register", userData);
      return response.data;
    } catch (error) {
      this.handleError(error);
      throw error;
    }
  }

  
  static async getAllUsers() {
  try {
    console.log("🔍 Vérification du rôle utilisateur...");
    if (!this.isAdmin()) {
      console.warn("⛔ Accès refusé : utilisateur non admin.");
      throw new Error("Accès refusé: Vous n'avez pas les droits administrateur");
    }

    console.log("🔍 Vérification du token...");
    const token = localStorage.getItem("token");
    console.log("📦 Token récupéré :", token);
    if (!token) {
      console.error("❌ Aucun token trouvé.");
      throw new Error("Session expirée ou invalide");
    }

    console.log("📡 Envoi de la requête vers /admin/get-all-users avec token...");
    const response = await this.apiClient.get("/admin/get-all-users", {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });

    console.log("✅ Réponse reçue avec le status :", response.status);

    if (response.status === 403) {
      console.error("🚫 Erreur 403 : Permissions insuffisantes");
      throw new Error("Permissions insuffisantes. Contactez votre administrateur.");
    }

    console.log("✅ Données des utilisateurs reçues :", response.data);
    return response.data;
  } catch (error) {
    console.error("🔥 Erreur dans getAllUsers:", error.message);
    this.handleError(error);
    throw error;
  }
}


  static async getYourProfile() {
    try {
      const response = await this.apiClient.get("/adminuser/get-profile", {
        validateStatus: (status) => status < 500,
      });
      return response.data;
    } catch (error) {
      this.handleError(error, "Failed to fetch profile");
      throw error;
    }
  }

  static async getUserById(userId) {
    try {
      const response = await this.apiClient.get(`/admin/get-user/${userId}`, {
        validateStatus: (status) => status < 500,
      });
      return response.data;
    } catch (error) {
      this.handleError(error, "Failed to fetch user");
      throw error;
    }
  }

  static async deleteUser(userId) {
    try {
      const response = await this.apiClient.delete(`/admin/delete/${userId}`, {
        validateStatus: (status) => status < 500,
      });
      return response.data;
    } catch (error) {
      this.handleError(error, "Failed to delete user");
      throw error;
    }
  }

  static async updateUser(userId, userData) {
    try {
      const response = await this.apiClient.put(`/admin/update/${userId}`, userData, {
        validateStatus: (status) => status < 500,
      });
      return response.data;
    } catch (error) {
      this.handleError(error, "Failed to update user");
      throw error;
    }
  }

  /** AUTHENTICATION CHECKER */
  static logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("userId");
  }

  static isAuthenticated() {
    return !!localStorage.getItem("token");
  }

  static isAdmin() {
    return localStorage.getItem("role") === "ADMIN";
  }

  static isUser() {
    return localStorage.getItem("role") === "USER";
  }

  static adminOnly() {
    return this.isAuthenticated() && this.isAdmin();
  }

  // Gestion centralisée des erreurs
  static handleError(error, customMessage = "") {
    if (error.response) {
      console.error(`Server error: ${error.response.status}`, error.response.data);
    } else if (error.request) {
      console.error("No response received", error.request);
    } else {
      console.error("Request setup error", error.message);
    }

    if (customMessage) {
      error.userMessage = customMessage;
      console.error("User message:", customMessage);
    }
  }
}

// Initialisation unique au chargement
UsersService.init();

export default UsersService;
