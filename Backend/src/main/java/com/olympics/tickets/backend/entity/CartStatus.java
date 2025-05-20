package com.olympics.tickets.backend.entity;

public enum CartStatus {
    ACTIVE,
    PAID,       // <-- ajoute cette ligne si tu veux ce statut
    COMPLETED,
    CANCELLED
}