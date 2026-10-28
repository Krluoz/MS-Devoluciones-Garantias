package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.WarrantyStatus;

public record WarrantyDecisionRequest(
        WarrantyStatus decision,
        WarrantyStatus status,
        String reason
) {
    public WarrantyStatus getEffectiveDecision() {
        return decision != null ? decision : status;
    }
}