package com.olympics.tickets.backend.controller;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import com.olympics.tickets.backend.entity.OurUsers;
import com.olympics.tickets.backend.exception.NotFoundException;
import com.olympics.tickets.backend.service.CartService;
import com.olympics.tickets.backend.repository.UsersRepo;
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

    @GetMapping
    public ResponseEntity<CartDTO> getCart(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO cart = cartService.getUserCart(userId);
            return ResponseEntity.ok(cart);
        } catch (NotFoundException e) {
            log.warn("Utilisateur non trouvé ou non authentifié: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du panier", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(Authentication authentication,
                                                 @RequestBody CartItemDTO cartItemDTO) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO updatedCart = cartService.addItemToCart(userId, cartItemDTO);
            return ResponseEntity.ok(updatedCart);
        } catch (NotFoundException e) {
            log.warn("Utilisateur non trouvé ou non authentifié: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout d'un article au panier", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateCartItem(Authentication authentication,
                                                  @PathVariable Long itemId,
                                                  @RequestBody CartItemDTO cartItemDTO) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO updatedCart = cartService.updateCartItemQuantity(userId, itemId, cartItemDTO.getQuantity());
            return ResponseEntity.ok(updatedCart);
        } catch (NotFoundException e) {
            log.warn("Utilisateur ou article non trouvé: {}", e.getMessage());
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la quantité", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> deleteCartItem(Authentication authentication,
                                                  @PathVariable Long itemId) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO updatedCart = cartService.deleteCartItem(userId, itemId);
            return ResponseEntity.ok(updatedCart);
        } catch (NotFoundException e) {
            log.warn("Utilisateur ou article non trouvé: {}", e.getMessage());
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'article", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<CartDTO> clearCart(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO emptyCart = cartService.clearUserCart(userId);
            return ResponseEntity.ok(emptyCart);
        } catch (NotFoundException e) {
            log.warn("Utilisateur non trouvé ou non authentifié: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Erreur lors du vidage du panier", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCart(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO validatedCart = cartService.validateCart(userId);
            return ResponseEntity.ok(validatedCart);
        } catch (NotFoundException e) {
            log.warn("Utilisateur non trouvé ou panier vide: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Utilisateur non trouvé ou panier vide");
        } catch (IllegalStateException e) {
            log.warn("Validation impossible: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation impossible : " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur lors de la validation du panier", e);
            return ResponseEntity.internalServerError().build();
        }
    }


    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotFoundException("Utilisateur non authentifié");
        }
        String email = authentication.getName();
        OurUsers user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        return user.getId();
    }
}
