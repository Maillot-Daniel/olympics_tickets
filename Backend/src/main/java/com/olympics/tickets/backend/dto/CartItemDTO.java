package com.olympics.tickets.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemDTO {
    private Long id;

    @NotNull
    private Long eventId;

    private String eventTitle;
    private Integer offerTypeId;
    private String offerTypeName;

    @Min(1)
    private Integer quantity;

    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Alias pour compatibilité avec l'appel attendu ailleurs
    public String getOfferTypeDisplayName() {
        return offerTypeName != null ? offerTypeName : "Standard";
    }

    // Optionnel : ancienne méthode si utilisée
    public String getOfferType() {
        return offerTypeName != null ? offerTypeName : "Standard";
    }

    public boolean isTotalPriceValid() {
        if (this.unitPrice == null || this.quantity == null || this.quantity <= 0) {
            return false;
        }
        BigDecimal calculatedTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return calculatedTotal.compareTo(totalPrice) == 0;
    }
}
