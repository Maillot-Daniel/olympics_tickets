package com.olympics.tickets.backend.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List; // Interface List


public enum CartStatus {
    ACTIVE, COMPLETED, ABANDONED
}

public enum OrderStatus {
    PENDING,       // Commande créée mais non payée
    PAID,          // Paiement confirmé
    PROCESSING,    // En préparation
    SHIPPED,       // Expédiée
    DELIVERED,     // Livrée
    CANCELLED,     // Annulée
    REFUNDED  // Remboursee
}

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private OurUsers user;

    private LocalDateTime orderDate;
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    private BigDecimal totalAmount;
    // getters/setters
}

@Entity
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private OfferType offerType;

    private int quantity;
    private BigDecimal unitPrice;
    // getters/setters
}