package com.olympics.tickets.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Event event;

    @ManyToOne
    private OfferType offerType;

    private int quantity;

    private BigDecimal unitPrice;
}

