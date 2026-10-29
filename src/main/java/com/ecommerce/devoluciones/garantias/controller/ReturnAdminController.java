
package com.ecommerce.devoluciones.garantias.controller;

import com.ecommerce.devoluciones.garantias.dto.ReturnDecisionRequest;
import com.ecommerce.devoluciones.garantias.service.ReturnRequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/v1/admin/returns")
public class ReturnAdminController {

    private final ReturnRequestService returnRequestService;

    public ReturnAdminController(
            ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }


    @PatchMapping("/{id}/decision")
    public ResponseEntity<Void> decidirDevolucion(
            @PathVariable Long id,
            @Valid @RequestBody ReturnDecisionRequest request,
            Authentication authentication) {

        // Obtener el nombre del administrador autenticado
        String administrador = authentication.getName();

        // Enviar la decisión y el administrador al servicio
        returnRequestService.decidirDevolucion(
                id,
                request.decision(),
                administrador
        );

        return ResponseEntity.noContent().build();
    }

}
