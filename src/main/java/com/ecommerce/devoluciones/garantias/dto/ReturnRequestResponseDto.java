package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public record ReturnRequestResponseDto(
        Long id,
        Long orderId,
        Long userId,
        Status status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ReturnItemResponseDto> items
) {
}
