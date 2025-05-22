package com.olympics.tickets.backend.entity.enums;

public enum OrderStatus {
    PENDING,       // Commande créée mais non payée
    PAID,          // Paiement confirmé
    PROCESSING,    // En préparation
    SHIPPED,       // Expédiée
    DELIVERED,     // Livrée
    CANCELLED,     // Annulée
    REFUNDED  // Remboursee

}
