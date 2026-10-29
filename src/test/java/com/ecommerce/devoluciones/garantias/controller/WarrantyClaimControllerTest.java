package com.ecommerce.devoluciones.garantias.controller;

import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimRequest;
import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimResponse;
import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimStatusResponse;
import com.ecommerce.devoluciones.garantias.dto.WarrantyDecisionRequest;
import com.ecommerce.devoluciones.garantias.model.WarrantyStatus;
import com.ecommerce.devoluciones.garantias.service.WarrantyClaimService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.ecommerce.devoluciones.garantias.security.AuthenticatedUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarrantyClaimControllerTest {

    private WarrantyClaimService service;
    private WarrantyClaimController controller;

    @BeforeEach
    void setUp() {
        service = mock(WarrantyClaimService.class);
        controller = new WarrantyClaimController(service);
    }
    @BeforeEach
    void setUpHttpRequest() {
        MockHttpServletRequest httpRequest =
                new MockHttpServletRequest("POST", "/api/v1/warranties/claims");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
    }

    @AfterEach
    void clearHttpRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    private Authentication auth(String username, String role) {
        return new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority(role)));
    }

    @Test
    void createClaim_devuelve201ConElReclamoCreado() {
        WarrantyClaimRequest request = new WarrantyClaimRequest(100L, List.of());
        WarrantyClaimResponse response = new WarrantyClaimResponse(
                9L, 100L, 1L, WarrantyStatus.PENDIENTE, LocalDateTime.now(), List.of());
        when(service.createClaim(1L, request)).thenReturn(response);

        ResponseEntity<WarrantyClaimResponse> result =
                controller.createClaim(request, auth("1", "ROLE_USER"));

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        assertNotNull(result.getHeaders().getLocation());
        assertTrue(result.getHeaders().getLocation().toString()
                .endsWith("/api/v1/warranties/claims/9"));
        verify(service).createClaim(1L, request);
    }

    @Test
    void getClaimStatus_devuelve200ConElEstado() {
        WarrantyClaimStatusResponse response =
                new WarrantyClaimStatusResponse(9L, WarrantyStatus.PENDIENTE, LocalDateTime.now());
        when(service.getClaimStatus(9L, new AuthenticatedUser(1L, false))).thenReturn(response);

        ResponseEntity<WarrantyClaimStatusResponse> result =
                controller.getClaimStatus(9L, auth("1", "ROLE_USER"));

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
    }

    @Test
    void resolveClaim_devuelve200ConLaDecision() {
        WarrantyDecisionRequest request =
                new WarrantyDecisionRequest(WarrantyStatus.APROBADO, null, "Cumple condiciones");
        WarrantyClaimStatusResponse response =
                new WarrantyClaimStatusResponse(9L, WarrantyStatus.APROBADO, LocalDateTime.now());
        when(service.resolveClaim(9L, request)).thenReturn(response);

        ResponseEntity<WarrantyClaimStatusResponse> result = controller.resolveClaim(9L, request);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(WarrantyStatus.APROBADO, result.getBody().status());
        verify(service).resolveClaim(9L, request);
    }
}

