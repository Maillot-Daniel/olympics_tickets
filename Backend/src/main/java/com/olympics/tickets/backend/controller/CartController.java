package com.olympics.tickets.backend.controller;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.entity.OurUsers;
import com.olympics.tickets.backend.exception.NotFoundException;
import com.olympics.tickets.backend.repository.UsersRepo;
import com.olympics.tickets.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final UsersRepo usersRepo;

    @DeleteMapping("/clear")
    public ResponseEntity<CartDTO> clearCart(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuthentication(authentication);
            CartDTO emptyCart = cartService.clearUserCart(userId); // doit retourner un CartDTO
            return ResponseEntity.ok(emptyCart);
        } catch (NotFoundException e) {
            log.warn("Utilisateur non trouvé ou non authentifié: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors du vidage du panier", e);
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
