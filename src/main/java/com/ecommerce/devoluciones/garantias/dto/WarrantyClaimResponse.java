package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.WarrantyClaim;
import com.ecommerce.devoluciones.garantias.model.WarrantyStatus;

import java.time.LocalDateTime;
import java.util.List;

public record WarrantyClaimResponse(
        Long id,
        Long orderId,
        Long userId,
        WarrantyStatus status,
        LocalDateTime createdAt,
        List<WarrantyItemResponse> items
) {

    public static WarrantyClaimResponse from(WarrantyClaim claim) {
        return new WarrantyClaimResponse(
                claim.getId(),
                claim.getOrderId(),
                claim.getUserId(),
                claim.getStatus(),
                claim.getCreatedAt(),
                claim.getItems().stream()
                        .map(WarrantyItemResponse::from)
                        .toList()
        );
    }
}