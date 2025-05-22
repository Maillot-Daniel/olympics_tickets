package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import com.olympics.tickets.backend.entity.*;
import com.olympics.tickets.backend.entity.enums.CartStatus;
import com.olympics.tickets.backend.entity.enums.OrderStatus;
import com.olympics.tickets.backend.exception.*;
import com.olympics.tickets.backend.repository.*;
import jakarta.transaction.Transactional;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements ImplCartService {

    private static final String CART_NOT_FOUND_MSG = "Panier actif introuvable pour l'utilisateur ID: ";
    private static final String EMPTY_CART_MSG = "Impossible de valider un panier vide";
    private static final String INVALID_USER_MSG = "ID utilisateur invalide";
    private static final String STOCK_INSUFFISANT_MSG = "Stock insuffisant pour l'événement ID: ";

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final EventRepository eventRepository;

    @Override
    public void validateCart(Long userId) {
        validateUserId(userId);
        Cart cart = getActiveUserCart(userId);
        validateCartContent(cart);
        verifyStockAvailability(cart);

        Order order = createAndSaveOrder(cart);
        updateCartStatus(cart, CartStatus.PAID);
        // Pas de retour
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new InvalidInputException(INVALID_USER_MSG);
        }
    }

    /**
     * Récupère le panier actif d'un utilisateur
     * @param userId ID de l'utilisateur (non null)
     * @return Le panier actif de l'utilisateur
     * @throws ResourceNotFoundException si aucun panier actif n'est trouvé
     * @throws IllegalArgumentException si userId est null
     */
    private Cart getActiveUserCart(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("L'ID utilisateur ne peut pas être null");
        }

        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Aucun panier actif trouvé pour l'utilisateur ID %d", userId)
                ));
    }

    private void validateCartContent(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessValidationException(EMPTY_CART_MSG);
        }
    }

    private void verifyStockAvailability(Cart cart) {
        cart.getItems().forEach(item -> {
            if (item.getQuantity() > item.getEvent().getRemainingTickets()) {
                throw new InsufficientStockException(
                        STOCK_INSUFFISANT_MSG + item.getEvent().getId() +
                                ". Disponible: " + item.getEvent().getRemainingTickets() +
                                ", Demandé: " + item.getQuantity()
                );
            }
        });
    }

    private Order createAndSaveOrder(Cart cart) {
        Order order = buildOrderFromCart(cart);
        updateEventStocks(cart);
        return orderRepository.save(order);
    }

    private Order buildOrderFromCart(Cart cart) {
        return Order.builder()
                .user(cart.getUser())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PAID)
                .items(convertCartItemsToOrderItems(cart.getItems()))
                .totalAmount(calculateOrderTotal(cart.getItems()))
                .build();
    }

    private List<OrderItem> convertCartItemsToOrderItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::convertToOrderItem)
                .toList();
    }

    private OrderItem convertToOrderItem(CartItem cartItem) {
        return OrderItem.builder()
                .event(cartItem.getEvent())
                .offerType(cartItem.getOfferType())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .build();
    }

    private void updateEventStocks(Cart cart) {
        cart.getItems().forEach(item -> {
            Event event = item.getEvent();
            event.setRemainingTickets(event.getRemainingTickets() - item.getQuantity());
            eventRepository.save(event);
        });
    }

    private BigDecimal calculateOrderTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateCartStatus(Cart cart, CartStatus status) {
        cart.setActive(false);
        cart.setStatus(status);
        cartRepository.save(cart);
    }


    private CartDTO buildCompleteCartResponse(Cart cart, Order order) {
        Objects.requireNonNull(cart, "Le panier ne peut pas être null");
        Objects.requireNonNull(order, "La commande ne peut pas être null");

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .status(cart.getStatus().toString())
                .orderId(order.getId())
                .orderDate(order.getCreationDate())  // si c’est déjà LocalDateTime
                .items(mapCartItemsToDTOs(cart.getItems()))
                .totalPrice(calculateCartTotal(cart.getItems()))
                .build();
    }


    private List<CartItemDTO> mapCartItemsToDTOs(List<CartItem> items) {
        return items.stream()
                .map(this::buildCartItemDTO)
                .toList();
    }

    private CartItemDTO buildCartItemDTO(CartItem item) {
        return CartItemDTO.builder()
                .id(item.getId())
                .eventId(item.getEvent().getId())
                .eventTitle(item.getEvent().getTitle())
                .offerTypeId(item.getOfferType().getId())
                .offerTypeName(item.getOfferType().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(calculateItemTotal(item))
                .build();
    }

    private BigDecimal calculateItemTotal(CartItem item) {
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private BigDecimal calculateCartTotal(List<CartItem> items) {
        return items.stream()
                .map(this::calculateItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}