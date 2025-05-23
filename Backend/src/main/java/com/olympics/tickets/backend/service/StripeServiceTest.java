package com.olympics.tickets.backend.service;

import com.olympics.tickets.backend.dto.CartItemDTO;
import com.olympics.tickets.backend.repository.CartRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StripeServiceTest {

    private StripeService stripeService;
    private CartRepository cartRepository = mock(CartRepository.class);

    @BeforeEach
    void setUp() {
        stripeService = new StripeService(cartRepository);

        // Injecte les propriétés manuellement
        ReflectionTestUtils.setField(stripeService, "stripeSecretKey", "sk_test_123");
        ReflectionTestUtils.setField(stripeService, "successUrl", "http://localhost/success");
        ReflectionTestUtils.setField(stripeService, "cancelUrl", "http://localhost/cancel");
    }

    @Test
    void testCreateLineItem() {
        CartItemDTO dto = new CartItemDTO();
        dto.setEventTitle("Paris 2024");
        dto.setOfferType("VIP");
        dto.setUnitPrice(BigDecimal.valueOf(120.50));
        dto.setQuantity(2);

        SessionCreateParams.LineItem item = stripeService
                .getClass()
                .getDeclaredMethod("createLineItem", CartItemDTO.class)
                .invoke(stripeService, dto);

        assertNotNull(item);
        assertEquals(2L, item.getQuantity());
    }

    @Test
    void testCreateCheckoutSession() throws StripeException {
        CartItemDTO dto = new CartItemDTO();
        dto.setEventTitle("Paris 2024");
        dto.setOfferType("VIP");
        dto.setUnitPrice(BigDecimal.valueOf(100));
        dto.setQuantity(1);

        Session mockSession = mock(Session.class);
        when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/session123");

        try (MockedStatic<Session> sessionStatic = Mockito.mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class))).thenReturn(mockSession);

            String url = stripeService.createCheckoutSession(List.of(dto));
            assertEquals("https://checkout.stripe.com/session123", url);
        }
    }
}
