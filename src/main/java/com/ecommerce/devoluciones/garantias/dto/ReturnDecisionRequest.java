
package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.Status;
import jakarta.validation.constraints.NotNull;

public record ReturnDecisionRequest(
        @NotNull(message = "La decisión es obligatoria")
        Status decision
) {
}
