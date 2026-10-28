package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.ReturnReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReturnItemRequestDto(

        @NotNull(message = "El ID del artículo es obligatorio.")
        @Positive(message = "El ID del artículo no es valido.")
        Integer itemId,

        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor que cero.")
        Integer quantity,

        @NotNull(message = "El motivo de la devolución es obligatorio.")
        ReturnReason reason

) {
}
