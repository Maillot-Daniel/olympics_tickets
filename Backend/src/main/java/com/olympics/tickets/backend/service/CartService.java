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
import java.util.ArrayList;
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
        // 1. Vérifier la disponibilité des billets
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new NotFoundException("Événement non trouvé"));

        if (event.getRemainingTickets() < dto.getQuantity()) {
            throw new IllegalStateException("Quantité demandée non disponible");
        }

        // 2. Ajouter l'article au panier
        CartItem item = addToCart(dto, userId);

        // 3. Mettre à jour le stock temporairement
        event.setRemainingTickets(event.getRemainingTickets() - dto.getQuantity());
        eventRepository.save(event);

        // 4. Retourner le DTO complet du panier
        return convertToDTO(item.getCart());
    }

    @Transactional
    protected CartItem addToCart(CartItemDTO dto, Long userId) {
        // Trouver ou créer le panier
        Cart cart = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> createNewCart(userId));

        // Vérifier si l'article existe déjà dans le panier
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getEvent().getId().equals(dto.getEventId())
                        && i.getOfferType().getId().equals(dto.getOfferTypeId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Mise à jour de la quantité si l'article existe déjà
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + dto.getQuantity());
            return cartItemRepository.save(item);
        } else {
            // Création d'un nouvel item
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
    public void removeItemFromCart(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Article de panier non trouvé"));

        // Vérifier que l'article appartient bien à l'utilisateur
        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new SecurityException("Non autorisé à modifier ce panier");
        }

        // Restaurer le stock
        Event event = item.getEvent();
        event.setRemainingTickets(event.getRemainingTickets() + item.getQuantity());
        eventRepository.save(event);

        // Supprimer l'article
        cartItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public Cart findActiveCartByUserId(Long userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun panier actif trouvé"));
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

    private Cart createNewCart(Long userId) {
        OurUsers user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus(CartStatus.ACTIVE);
        return cartRepository.save(cart);
    }
}