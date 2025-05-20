package com.olympics.tickets.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import com.olympics.tickets.backend.service.CartService;
import com.olympics.tickets.backend.entity.OurUsers;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCurrentUserCart(Authentication authentication) {
        OurUsers user = (OurUsers) authentication.getPrincipal();
        CartDTO cart = cartService.getUserCart(user.getId());
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/active")
    public ResponseEntity<CartDTO> getActiveCart(Authentication authentication) {
        OurUsers user = (OurUsers) authentication.getPrincipal();
        CartDTO activeCart = cartService.findActiveCartByUserId(user.getId());

        if (activeCart == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(activeCart);
    }

    @PostMapping
    public ResponseEntity<CartDTO> createNewCart(Authentication authentication) {
        OurUsers user = (OurUsers) authentication.getPrincipal();
        CartDTO newCart = cartService.createNewCartAndReturnDTO(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newCart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(
            @Valid @RequestBody CartItemDTO itemDTO,
            Authentication authentication) {
        OurUsers user = (OurUsers) authentication.getPrincipal();
        try {
            CartDTO updatedCart = cartService.addItemToCart(user.getId(), itemDTO);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDTO> getUserCartById(
            @PathVariable Long userId,
            Authentication authentication) {
        OurUsers currentUser = (OurUsers) authentication.getPrincipal();
        if (!"ADMIN".equals(currentUser.getRole()) && !currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CartDTO cart = cartService.getUserCart(userId);
        if (cart == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(
            @PathVariable Long itemId,
            Authentication authentication) {
        OurUsers user = (OurUsers) authentication.getPrincipal();
        boolean removed = cartService.removeItemFromCart(user.getId(), itemId);
        if (removed) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
