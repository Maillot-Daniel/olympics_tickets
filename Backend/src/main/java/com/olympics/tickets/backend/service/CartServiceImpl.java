package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.olympics.tickets.backend.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

// NOTE: Implémentation simplifiée en mémoire.
// En prod, il faudra gérer en base avec des repository.

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    // Simulé pour l'exemple
    private final InMemoryCartStorage cartStorage;

    @Override
    public CartDTO getUserCart(Long userId) {
        return cartStorage.getCartForUser(userId);
    }

    @Override
    public CartDTO addItemToCart(Long userId, CartItemDTO cartItemDTO) {
        return cartStorage.addItem(userId, cartItemDTO);
    }

    @Override
    public CartDTO updateCartItemQuantity(Long userId, Long itemId, int quantity) {
        return cartStorage.updateItemQuantity(userId, itemId, quantity);
    }

    @Override
    public CartDTO deleteCartItem(Long userId, Long itemId) {
        return cartStorage.deleteItem(userId, itemId);
    }

    @Override
    public CartDTO clearUserCart(Long userId) {
        return cartStorage.clearCart(userId);
    }

    @Override
    public CartDTO validateCart(Long userId) {
        CartDTO cart = cartStorage.getCartForUser(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new NotFoundException("Panier vide, impossible de valider");
        }
        // Ici, tu peux ajouter d'autres validations (stocks, paiement, etc.)

        cartStorage.clearCart(userId);  // Vide le panier après validation
        return cart;
    }
}
