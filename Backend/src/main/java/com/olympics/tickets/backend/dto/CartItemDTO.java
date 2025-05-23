package com.olympics.tickets.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id; // id de l'item dans le panier (peut être null à la création)
    private Long eventId;
    private Long offerTypeId;
    private int quantity;
    private double pricePerUnit;
}
