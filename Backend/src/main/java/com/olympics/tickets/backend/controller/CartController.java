package com.olympics.tickets.backend.controller;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import com.olympics.tickets.backend.entity.OurUsers;
import com.olympics.tickets.backend.exception.NotFoundException;
import com.olympics.tickets.backend.repository.UsersRepo;
import com.olympics.tickets.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final UsersRepo usersRepo;

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(@RequestBody CartItemDTO itemDTO, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO updatedCart = cartService.addItemToCart(userId, itemDTO);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout d'un item au panier", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<CartDTO> getActiveCart(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO cart = cartService.findActiveCartByUserId(userId);
            if (cart == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du panier actif", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long itemId, Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            boolean removed = cartService.removeItemFromCart(userId, itemId);
            if (removed) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'item du panier", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Void> validateCart(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            cartService.validateCart(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur lors de la validation du panier", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🔐 Récupération de l'utilisateur via l'email d'authentification
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("Utilisateur non authentifié");
        }

        String email = authentication.getName();
        log.debug("Récupération de l'utilisateur avec email: {}", email);

        OurUsers user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec email: " + email));

        return user.getId();
    }
}
