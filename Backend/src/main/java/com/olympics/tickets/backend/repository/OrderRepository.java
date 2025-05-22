package com.olympics.tickets.backend.repository;

import com.olympics.tickets.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Vous pouvez ajouter des méthodes personnalisées si nécessaire
    // Par exemple :
    // List<Order> findByUserId(Long userId);
}

