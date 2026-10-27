package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.Status;

import java.time.LocalDateTime;

public record ReturnSummaryDTO(
        Long id,
        Long orderId,
        Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}