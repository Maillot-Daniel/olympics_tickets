package com.olympics.tickets.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import com.olympics.tickets.backend.service.CartService;
import com.olympics.tickets.backend.entity.OurUsers;
import com.olympics.tickets.backend.entity.Cart;
import com.olympics.tickets.backend.security.JwtUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final JwtUtils jwtTokenUtil;

    // Récupère le panier de l'utilisateur connecté
    @GetMapping
    public ResponseEntity<CartDTO> getCurrentUserCart(Authentication authentication) {
        OurUsers user = (OurUsers) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.getUserCart(user.getId()));
    }

    @GetMapping("/active")
    public ResponseEntity<CartDTO> getActiveCart(@RequestHeader("Authorization") String token) {
        // 1. Valider le token et extraire userId
        Long userId = jwtTokenUtil.getUserIdFromToken(token.replace("Bearer ", ""));

        // 2. Chercher le panier actif
        CartDTO activeCart = cartService.findActiveCartByUserId(userId);

        if (activeCart == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(activeCart);
    }

    // Ajoute un item au panier de l'utilisateur connecté
    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(
            @Valid @RequestBody CartItemDTO itemDTO,
            Authentication authentication) {
        OurUsers user = (OurUsers) authentication.getPrincipal();
        return ResponseEntity.ok(cartService.addItemToCart(user.getId(), itemDTO));
    }

    // Endpoint admin pour récupérer le panier d'un utilisateur spécifique
    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDTO> getUserCartById(
            @PathVariable Long userId,
            Authentication authentication) {
        // Vérifier que l'utilisateur est admin ou accède à son propre panier
        OurUsers currentUser = (OurUsers) authentication.getPrincipal();
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(cartService.getUserCart(userId));
    }

    // Supprime un item du panier
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long itemId,
            Authentication authentication) {
        OurUsers user = (OurUsers) authentication.getPrincipal();
        cartService.removeItemFromCart(user.getId(), itemId);
        return ResponseEntity.noContent().build();
    }
}