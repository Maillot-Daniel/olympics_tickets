package com.olympics.tickets.backend.mapper;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import com.olympics.tickets.backend.entity.Cart;
import com.olympics.tickets.backend.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {

    public static CartDTO toDTO(Cart cart) {
        if (cart == null) return null;

        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        dto.setStatus(cart.getStatus().name());

        List<CartItemDTO> itemDTOs = cart.getItems() != null
                ? cart.getItems().stream().map(CartMapper::toDTO).collect(Collectors.toList())
                : List.of();

        dto.setItems(itemDTOs);

        BigDecimal totalPrice = itemDTOs.stream()
                .map(CartItemDTO::getTotalPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        dto.setTotalPrice(totalPrice);

        return dto;
    }

    public static CartItemDTO toDTO(CartItem item) {
        if (item == null) return null;

        return CartItemDTO.builder()
                .id(item.getId())
                .eventId(item.getEvent() != null ? item.getEvent().getId() : null)
                .eventTitle(item.getEvent() != null ? item.getEvent().getTitle() : null)
                .offerTypeId(item.getOfferType() != null ? item.getOfferType().getId() : null)
                .offerTypeName(item.getOfferType() != null ? item.getOfferType().getName() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
