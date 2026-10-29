package com.ecommerce.devoluciones.garantias.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record WarrantyClaimRequest(

        @NotNull(message = "El id de la orden es obligatorio")
        @Positive(message = "El id de la orden debe ser positivo")
        Long orderId,

        @NotEmpty(message = "Debe incluir al menos un producto")
        @Valid
        List<WarrantyItemRequest> items
) {
}