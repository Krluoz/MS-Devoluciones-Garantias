package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.WarrantyItem;
import com.ecommerce.devoluciones.garantias.model.WarrantyReason;

import java.time.LocalDateTime;

public record WarrantyItemResponse(
        Long id,
        Long itemId,
        String itemName,
        Integer quantity,
        WarrantyReason reason,
        String description,
        LocalDateTime purchaseDate,
        LocalDateTime warrantyExpiresAt
) {

    public static WarrantyItemResponse from(WarrantyItem item) {
        return new WarrantyItemResponse(
                item.getId(),
                item.getItemId(),
                item.getItemName(),
                item.getQuantity(),
                item.getReason(),
                item.getDescription(),
                item.getPurchaseDate(),
                item.getWarrantyExpiresAt()
        );
    }
}