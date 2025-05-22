package com.olympics.tickets.backend.dto;

import com.olympics.tickets.backend.entity.Cart;
import com.olympics.tickets.backend.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CartBuildDTO {

    // Méthode pour convertir un CartItem en CartItemDTO
    private CartItemDTO buildCartItemDTO(CartItem item) {
        return CartItemDTO.builder()
                .id(item.getId())
                .eventId(item.getEvent().getId())
                .offerTypeId(item.getOfferType().getId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    // Méthode pour convertir un Cart en CartDTO
    public CartDTO buildCartDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems() != null
                ? cart.getItems().stream()
                .map(this::buildCartItemDTO)
                .collect(Collectors.toList())
                : List.of();

        BigDecimal totalPrice = items.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .status(cart.getStatus().name())
                .items(items)
                .totalPrice(totalPrice)
                .build();
    }
}
