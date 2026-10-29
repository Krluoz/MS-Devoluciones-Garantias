package com.ecommerce.devoluciones.garantias.controller;

import com.ecommerce.devoluciones.garantias.dto.ReturnDecisionRequest;
import com.ecommerce.devoluciones.garantias.model.Status;
import com.ecommerce.devoluciones.garantias.service.ReturnRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReturnAdminControllerTest {

    private ReturnRequestService service;
    private ReturnAdminController controller;

    @BeforeEach
    void setUp() {
        service = mock(ReturnRequestService.class);
        controller = new ReturnAdminController(service);
    }

    @Test
    void decidirDevolucion_pasaDecisionYAdministradorYResponde204() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin-pruebas");
        ReturnDecisionRequest request = new ReturnDecisionRequest(Status.APROBADO);

        ResponseEntity<Void> response = controller.decidirDevolucion(12L, request, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).decidirDevolucion(12L, Status.APROBADO, "admin-pruebas");
    }
}
