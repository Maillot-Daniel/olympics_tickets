package com.olympics.tickets.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long userId;
    private List<CartItemDTO> items;
    private double totalPrice;
}
