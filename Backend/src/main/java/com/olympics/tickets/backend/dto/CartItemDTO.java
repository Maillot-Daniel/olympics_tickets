package com.olympics.tickets.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {

    private Long id;

    private Long cartId;

    @NotNull(message = "L'ID de l'événement ne peut pas être nul")
    private Long eventId;

    private String eventTitle;

    @NotNull(message = "Le type d'offre ne peut pas être nul")
    private Long offerTypeId;

    private String offerTypeName;

    @NotNull(message = "La quantité ne peut pas être nulle")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantity;

    @NotNull(message = "Le prix unitaire ne peut pas être nul")
    private BigDecimal unitPrice;

    @NotNull(message = "Le prix total ne peut pas être nul")
    private BigDecimal totalPrice;

    public String getOfferTypeDisplayName() {
        return this.offerTypeName != null ? this.offerTypeName : "Standard";
    }

    public boolean isValid() {
        if (unitPrice == null || quantity == null || quantity <= 0) return false;
        if (totalPrice == null) return false;

        BigDecimal expectedTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return expectedTotal.compareTo(totalPrice) == 0;
    }

    public void calculateAndSetTotalPrice() {
        if (unitPrice != null && quantity != null && quantity > 0) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
