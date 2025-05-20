package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.dto.CartDTO;
import com.olympics.tickets.backend.dto.CartItemDTO;
import com.olympics.tickets.backend.entity.*;
import com.olympics.tickets.backend.exception.NotFoundException;
import com.olympics.tickets.backend.exception.ResourceNotFoundException;
import com.olympics.tickets.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final EventRepository eventRepository;
    private final OfferTypeRepository offerTypeRepository;
    private final UsersRepo userRepository;

    @Transactional
    public CartDTO addItemToCart(Long userId, CartItemDTO dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new NotFoundException("Événement non trouvé"));

        if (event.getRemainingTickets() < dto.getQuantity()) {
            throw new IllegalStateException("Quantité demandée non disponible");
        }

        CartItem item = addToCart(dto, userId);

        event.setRemainingTickets(event.getRemainingTickets() - dto.getQuantity());
        eventRepository.save(event);

        return convertToDTO(item.getCart());
    }

    @Transactional
    protected CartItem addToCart(CartItemDTO dto, Long userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createNewCart(userId));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getEvent().getId().equals(dto.getEventId())
                        && i.getOfferType().getId().equals(dto.getOfferTypeId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + dto.getQuantity());
            return cartItemRepository.save(item);
        } else {
            OfferType offerType = offerTypeRepository.findById(dto.getOfferTypeId())
                    .orElseThrow(() -> new NotFoundException("Type d'offre non trouvé"));

            CartItem item = new CartItem();
            item.setCart(cart);
            item.setEvent(eventRepository.getReferenceById(dto.getEventId()));
            item.setOfferType(offerType);
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(calculatePrice(eventRepository.getReferenceById(dto.getEventId()).getPrice(), offerType.getName()));

            cart.getItems().add(item);
            return cartItemRepository.save(item);
        }
    }

    @Transactional(readOnly = true)
    public CartDTO getUserCart(Long userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Panier non trouvé"));
        return convertToDTO(cart);
    }

    @Transactional
    public boolean removeItemFromCart(Long userId, Long itemId) {
        Optional<CartItem> optItem = cartItemRepository.findById(itemId);
        if (optItem.isEmpty()) return false;

        CartItem item = optItem.get();

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new SecurityException("Non autorisé à modifier ce panier");
        }

        Event event = item.getEvent();
        event.setRemainingTickets(event.getRemainingTickets() + item.getQuantity());
        eventRepository.save(event);

        cartItemRepository.delete(item);
        return true;
    }

    @Transactional(readOnly = true)
    public CartDTO findActiveCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        return convertToDTO(cart);
    }

    @Transactional
    public CartDTO createNewCartAndReturnDTO(Long userId) {
        Cart cart = createNewCart(userId);
        return convertToDTO(cart);
    }

    // Méthode privée pour créer un panier
    private Cart createNewCart(Long userId) {
        OurUsers user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);
        return cartRepository.save(cart);
    }

    private CartDTO convertToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setStatus(cart.getStatus().toString());

        List<CartItemDTO> items = cart.getItems().stream()
                .map(this::convertItemToDTO)
                .toList();

        dto.setItems(items);
        dto.setTotalPrice(calculateTotalPrice(items));
        return dto;
    }

    private CartItemDTO convertItemToDTO(CartItem item) {
        return CartItemDTO.builder()
                .id(item.getId())
                .eventId(item.getEvent().getId())
                .eventTitle(item.getEvent().getTitle())
                .offerTypeId(item.getOfferType().getId())
                .offerTypeName(item.getOfferType().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    private BigDecimal calculateTotalPrice(List<CartItemDTO> items) {
        return items.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePrice(BigDecimal basePrice, String offerType) {
        return switch (offerType.toUpperCase()) {
            case "DUO" -> basePrice.multiply(BigDecimal.valueOf(1.9));
            case "FAMILLE" -> basePrice.multiply(BigDecimal.valueOf(3.5));
            default -> basePrice;
        };
    }
}
