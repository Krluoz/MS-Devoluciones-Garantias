package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.WarrantyClaim;
import com.ecommerce.devoluciones.garantias.model.WarrantyStatus;

import java.time.LocalDateTime;

public record WarrantyClaimStatusResponse(
        Long id,
        WarrantyStatus status,
        LocalDateTime updatedAt
) {

    public static WarrantyClaimStatusResponse from(WarrantyClaim claim) {
        return new WarrantyClaimStatusResponse(
                claim.getId(),
                claim.getStatus(),
                claim.getUpdatedAt()
        );
    }
}