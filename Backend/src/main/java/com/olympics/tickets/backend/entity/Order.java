package com.olympics.tickets.backend.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.olympics.tickets.backend.entity.enums.OrderStatus;

@Entity
@Getter
@Setter
@Builder
public class Order {
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private OurUsers user;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    private BigDecimal totalAmount;
}