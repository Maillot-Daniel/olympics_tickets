package com.olympics.tickets.backend.repository;

import com.olympics.tickets.backend.entity.Cart;
import com.olympics.tickets.backend.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    // Ajoutez aussi cette méthode si vous l'utilisez ailleurs
    Optional<Cart> findByUserIdAndActiveTrue(Long userId);
}
