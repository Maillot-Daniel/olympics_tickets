package com.olympics.tickets.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartDTO {
    private Long id;
    private Long userId;
    private String status;
    private List<CartItemDTO> items;
    private BigDecimal totalPrice;
    private Long orderId;
    private LocalDateTime orderDate;
}
