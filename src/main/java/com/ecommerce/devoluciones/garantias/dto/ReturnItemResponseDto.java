package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.ReturnReason;

public record ReturnItemResponseDto(
        Long id,
        Integer itemId,
        String itemName,
        Integer quantity,
        ReturnReason reason
) {
}
