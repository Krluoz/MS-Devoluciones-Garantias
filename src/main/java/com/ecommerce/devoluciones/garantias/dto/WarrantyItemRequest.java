package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.WarrantyReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WarrantyItemRequest(

        @NotNull(message = "El id del producto es obligatorio")
        @Positive(message = "El id del producto debe ser positivo")
        Long itemId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer quantity,

        @NotNull(message = "La razón es obligatoria")
        WarrantyReason reason,

        @Size(max = 500, message = "La descripción no puede pasar de 500 caracteres")
        String description
) {
}