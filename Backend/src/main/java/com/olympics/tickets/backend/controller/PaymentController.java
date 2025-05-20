package com.olympics.tickets.backend.controller;

import com.olympics.tickets.backend.dto.CartItemDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(
            @RequestBody @Valid CartRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new LinkedHashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                String fieldName = error.getField();
                if (fieldName.startsWith("cartItems[")) {
                    // Simplify field name for errors inside list
                    fieldName = "cartItems" + fieldName.substring(fieldName.indexOf("]"));
                }
                errors.put(fieldName, error.getDefaultMessage());
            });
            log.warn("Validation errors: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        List<CartItemDTO> cartItems = request.getCartItems();
        log.info("Processing payment for {} items", cartItems.size());

        // Validate each item and convert to Stripe line items
        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();
        List<String> validationErrors = new ArrayList<>();

        for (int i = 0; i < cartItems.size(); i++) {
            CartItemDTO item = cartItems.get(i);
            try {
                validateCartItem(item);
                lineItems.add(convertToStripeLineItem(item));
            } catch (IllegalArgumentException e) {
                validationErrors.add(String.format("Item %d: %s", i + 1, e.getMessage()));
            }
        }

        if (!validationErrors.isEmpty()) {
            log.warn("Item validation errors: {}", validationErrors);
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Certains articles sont invalides",
                    "errors", validationErrors
            ));
        }

        try {
            // Construire les paramètres de session Stripe
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendBaseUrl + "/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendBaseUrl + "/cancel");

            lineItems.forEach(paramsBuilder::addLineItem);

            if (request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty()) {
                paramsBuilder.setCustomerEmail(request.getCustomerEmail());
            }

            Session session = Session.create(paramsBuilder.build());
            log.info("Stripe session created: {}", session.getId());

            return ResponseEntity.ok(new CheckoutSessionResponse(
                    session.getId(),
                    session.getUrl(),
                    convertToEuros(session.getAmountTotal()),
                    "eur"
            ));

        } catch (StripeException e) {
            log.error("Stripe error: {}", e.getMessage(), e);
            String errorMessage = e.getStripeError() != null ?
                    e.getStripeError().getMessage() : "Erreur de traitement du paiement";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", errorMessage));
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Une erreur interne est survenue"));
        }
    }

    private void validateCartItem(CartItemDTO item) {
        if (item == null) {
            throw new IllegalArgumentException("L'article ne peut pas être null");
        }
        if (item.getEventId() == null) {
            throw new IllegalArgumentException("L'ID d'événement est requis");
        }
        if (item.getEventTitle() == null || item.getEventTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre de l'événement est requis");
        }
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Prix unitaire invalide");
        }
        BigDecimal expectedTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        if (item.getTotalPrice() == null || item.getTotalPrice().compareTo(expectedTotal) != 0) {
            throw new IllegalArgumentException("Le prix total ne correspond pas au prix unitaire × quantité");
        }
    }

    private SessionCreateParams.LineItem convertToStripeLineItem(CartItemDTO item) {
        long unitAmount = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact(); // Utiliser longValueExact pour éviter erreurs

        return SessionCreateParams.LineItem.builder()
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("eur")
                                .setUnitAmount(unitAmount)
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(item.getEventTitle() + " - " + item.getOfferTypeDisplayName())
                                                .build())
                                .build())
                .setQuantity((long) item.getQuantity())
                .build();
    }

    private BigDecimal convertToEuros(Long amountInCents) {
        if (amountInCents == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Data
    public static class CartRequest {
        @NotNull(message = "La liste des articles ne peut pas être nulle")
        @NotEmpty(message = "Le panier ne peut pas être vide")
        private List<@Valid CartItemDTO> cartItems;

        @Email(message = "L'email doit être valide")
        private String customerEmail;
    }

    @Data
    @AllArgsConstructor
    public static class CheckoutSessionResponse {
        private String sessionId;
        private String url;
        private BigDecimal amount;
        private String currency;
    }
}
