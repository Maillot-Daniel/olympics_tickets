package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InMemoryCartStorage {

    // Map userId -> List<CartItemDTO>
    private final Map<Long, List<CartItemDTO>> carts = new HashMap<>();

    public CartDTO getCartForUser(Long userId) {
        List<CartItemDTO> items = carts.getOrDefault(userId, new ArrayList<>());
        double total = items.stream().mapToDouble(i -> i.getPricePerUnit() * i.getQuantity()).sum();
        return new CartDTO(userId, items, total);
    }

    public CartDTO addItem(Long userId, CartItemDTO newItem) {
        List<CartItemDTO> items = carts.computeIfAbsent(userId, k -> new ArrayList<>());
        // Recherche si item identique (eventId + offerTypeId) existe déjà
        Optional<CartItemDTO> existingOpt = items.stream()
                .filter(i -> i.getEventId().equals(newItem.getEventId()) && i.getOfferTypeId().equals(newItem.getOfferTypeId()))
                .findFirst();

        if (existingOpt.isPresent()) {
            CartItemDTO existing = existingOpt.get();
            existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
        } else {
            // On simule l'id auto-increment (simple)
            long newId = items.stream().mapToLong(CartItemDTO::getId).max().orElse(0L) + 1;
            newItem.setId(newId);
            items.add(newItem);
        }
        return getCartForUser(userId);
    }

    public CartDTO updateItemQuantity(Long userId, Long itemId, int quantity) {
        List<CartItemDTO> items = carts.get(userId);
        if (items == null) {
            throw new NoSuchElementException("Panier introuvable");
        }
        CartItemDTO item = items.stream().filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Article introuvable"));
        if (quantity <= 0) {
            items.remove(item);
        } else {
            item.setQuantity(quantity);
        }
        return getCartForUser(userId);
    }

    public CartDTO deleteItem(Long userId, Long itemId) {
        List<CartItemDTO> items = carts.get(userId);
        if (items == null) {
            throw new NoSuchElementException("Panier introuvable");
        }
        items.removeIf(i -> i.getId().equals(itemId));
        return getCartForUser(userId);
    }

    public CartDTO clearCart(Long userId) {
        carts.remove(userId);
        return new CartDTO(userId, new ArrayList<>(), 0);
    }
}
