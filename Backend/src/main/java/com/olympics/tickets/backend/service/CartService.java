package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;

public interface CartService {

    CartDTO getUserCart(Long userId);

    CartDTO addItemToCart(Long userId, CartItemDTO cartItemDTO);

    CartDTO updateCartItemQuantity(Long userId, Long itemId, int quantity);

    CartDTO deleteCartItem(Long userId, Long itemId);

    CartDTO clearUserCart(Long userId);

    CartDTO validateCart(Long userId);

}
