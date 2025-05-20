package com.olympics.tickets.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_item")
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private OurUsers user;

    @ManyToOne
    @JoinColumn(name = "offer_type", referencedColumnName = "id", nullable = false)
    private OfferType offerType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal unitPrice;

    @Transient
    public BigDecimal getTotalPrice() {
        if (unitPrice != null && quantity > 0) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}
