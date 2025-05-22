package com.olympics.tickets.backend.repository;

import com.olympics.tickets.backend.entity.CartItem;
import com.olympics.tickets.backend.entity.OurUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Trouve tous les articles du panier pour un utilisateur donné
     * @param user L'utilisateur
     * @return Liste des articles du panier
     */
    List<CartItem> findByCartUser(OurUsers user);

    /**
     * Supprime tous les articles d'un panier spécifique
     * @param cartId L'ID du panier
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);

    /**
     * Trouve un article spécifique dans le panier d'un utilisateur
     * @param itemId ID de l'article
     * @param user L'utilisateur propriétaire du panier
     * @return Optional contenant l'article si trouvé
     */
    Optional<CartItem> findByIdAndCartUser(Long itemId, OurUsers user);

    /**
     * Compte le nombre d'articles dans le panier d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Le nombre d'articles
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}