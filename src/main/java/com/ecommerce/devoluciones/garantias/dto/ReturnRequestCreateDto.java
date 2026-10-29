package com.ecommerce.devoluciones.garantias.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReturnRequestCreateDto(

        @NotNull(message = "El ID del pedido es obligatorio.")
        @Positive(message = "El ID del pedido no es valido.")
        Long orderId,

        @NotNull(message = "El ID del usuario es obligatorio.")
        @Positive(message = "El ID del usuario no es valido.")
        Long userId,

        @NotNull(message = "Los artículos son obligatorios.")
        @Size(min = 1, message = "La solicitud debe contener al menos un artículo.")
        List<@Valid ReturnItemRequestDto> items

) {
}
