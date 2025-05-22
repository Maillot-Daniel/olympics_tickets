package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import com.olympics.tickets.backend.entity.*;
import com.olympics.tickets.backend.entity.enums.CartStatus;
import com.olympics.tickets.backend.exception.*;
import com.olympics.tickets.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private static final BigDecimal DUO_MULTIPLIER = BigDecimal.valueOf(1.9);
    private static final BigDecimal FAMILY_MULTIPLIER = BigDecimal.valueOf(3.5);
    private static final int MIN_QUANTITY = 1;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final EventRepository eventRepository;
    private final OfferTypeRepository offerTypeRepository;
    private final UsersRepo userRepository;

    public CartDTO addItemToCart(Long userId, CartItemDTO dto) {
        validateInput(userId, dto);
        Event event = getEventWithStockCheck(dto);
        Cart cart = getOrCreateActiveCart(userId);
        CartItem item = processCartItem(dto, event, cart);
        updateEventStock(event, dto.getQuantity());
        log.info("Added item {} to cart for user {}", item.getId(), userId);
        return buildCartDTO(cart);
    }

    @Transactional(readOnly = true)
    public CartDTO getUserCart(Long userId) {
        Cart cart = getActiveCartByUser(userId);
        return buildCartDTO(cart);
    }

    @Transactional
    public void removeCartItem(Long userId, Long itemId) {
        CartItem item = validateCartItemOwnership(userId, itemId);
        restoreEventStock(item);
        cartItemRepository.delete(item);
        log.info("Removed item {} from cart for user {}", itemId, userId);
    }

    @Transactional
    public void validateCart(Long userId) {
        Cart cart = getActiveCartByUser(userId);
        validateCartContent(cart);
        updateCartStatus(cart, CartStatus.PAID);
        log.info("Validated cart for user {}", userId);
    }

    /**
     * Méthode corrigée : vide le panier et retourne le DTO mis à jour
     */
    @Transactional
    public CartDTO clearUserCart(Long userId) {
        Cart cart = getActiveCartByUser(userId);
        cart.getItems().forEach(this::restoreEventStock);
        cartItemRepository.deleteAllByCartId(cart.getId());
        log.info("Cleared cart for user {}", userId);

        return buildCartDTO(cart);
    }



    private void validateInput(Long userId, CartItemDTO dto) {
        if (userId == null || dto == null) {
            throw new InvalidRequestException("Request parameters cannot be null");
        }
        if (dto.getQuantity() < MIN_QUANTITY) {
            throw new InvalidRequestException("Quantity must be at least " + MIN_QUANTITY);
        }
    }

    private Event getEventWithStockCheck(CartItemDTO dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + dto.getEventId()));

        if (event.getRemainingTickets() < dto.getQuantity()) {
            throw new InsufficientStockException("Not enough tickets available. Remaining: " + event.getRemainingTickets());
        }
        return event;
    }

    private Cart getOrCreateActiveCart(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    OurUsers user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(newCart);
                });
    }

    private CartItem processCartItem(CartItemDTO dto, Event event, Cart cart) {
        return findExistingCartItem(cart, dto)
                .map(existingItem -> updateItemQuantity(existingItem, dto.getQuantity()))
                .orElseGet(() -> createNewCartItem(dto, event, cart));
    }

    private Optional<CartItem> findExistingCartItem(Cart cart, CartItemDTO dto) {
        return cart.getItems().stream()
                .filter(i -> i.getEvent().getId().equals(dto.getEventId())
                        && i.getOfferType().getId().equals(dto.getOfferTypeId()))
                .findFirst();
    }

    private CartItem updateItemQuantity(CartItem item, int quantity) {
        item.setQuantity(item.getQuantity() + quantity);
        return cartItemRepository.save(item);
    }

    private CartItem createNewCartItem(CartItemDTO dto, Event event, Cart cart) {
        Long offerTypeId = dto.getOfferTypeId().longValue();

        OfferType offerType = offerTypeRepository.findById(offerTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("OfferType not found with id: " + offerTypeId));

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setEvent(event);
        item.setOfferType(offerType);
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(calculateItemPrice(event.getPrice(), offerType.getName()));

        cart.getItems().add(item);
        return cartItemRepository.save(item);
    }

    private void updateEventStock(Event event, int quantity) {
        int newStock = event.getRemainingTickets() - quantity;
        event.setRemainingTickets(newStock);
        eventRepository.save(event);
    }

    private Cart getActiveCartByUser(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active cart not found for user id: " + userId));
    }

    private CartItem validateCartItemOwnership(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("User " + userId + " not authorized to modify cart item " + itemId);
        }
        return item;
    }

    private void restoreEventStock(CartItem item) {
        Event event = item.getEvent();
        event.setRemainingTickets(event.getRemainingTickets() + item.getQuantity());
        eventRepository.save(event);
    }

    private void validateCartContent(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessValidationException("Cannot validate an empty cart");
        }
    }

    private void updateCartStatus(Cart cart, CartStatus status) {
        cart.setStatus(status);
        cartRepository.save(cart);
    }

    private CartDTO buildCartDTO(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(this::buildCartItemDTO)
                .collect(Collectors.toList());

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .status(cart.getStatus().name())
                .items(items)
                .totalPrice(calculateTotalPrice(items))
                .build();
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

    private BigDecimal calculateTotalPrice(List<CartItemDTO> items) {
        return items.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateItemPrice(BigDecimal basePrice, String offerType) {
        if (offerType == null) return basePrice;

        return switch (offerType.toUpperCase()) {
            case "DUO" -> basePrice.multiply(DUO_MULTIPLIER);
            case "FAMILLE" -> basePrice.multiply(FAMILY_MULTIPLIER);
            default -> basePrice;
        };
    }
}
