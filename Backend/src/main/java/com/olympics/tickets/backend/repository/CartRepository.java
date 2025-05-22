package com.olympics.tickets.backend.repository;

import com.olympics.tickets.backend.entity.Cart;
import com.olympics.tickets.backend.entity.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

}
