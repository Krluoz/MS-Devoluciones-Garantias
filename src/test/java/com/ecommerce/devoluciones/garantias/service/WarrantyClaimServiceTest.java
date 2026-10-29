package com.ecommerce.devoluciones.garantias.service;

import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimStatusResponse;
import com.ecommerce.devoluciones.garantias.dto.WarrantyDecisionRequest;
import com.ecommerce.devoluciones.garantias.exception.WarrantyBadRequestException;
import com.ecommerce.devoluciones.garantias.exception.WarrantyNotFoundException;
import com.ecommerce.devoluciones.garantias.exception.WarrantyRuleException;
import com.ecommerce.devoluciones.garantias.gateway.OrderGateway;
import com.ecommerce.devoluciones.garantias.model.WarrantyClaim;
import com.ecommerce.devoluciones.garantias.model.WarrantyItem;
import com.ecommerce.devoluciones.garantias.model.WarrantyReason;
import com.ecommerce.devoluciones.garantias.model.WarrantyStatus;
import com.ecommerce.devoluciones.garantias.repository.WarrantyClaimRepository;
import com.ecommerce.devoluciones.garantias.repository.WarrantyItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WarrantyClaimServiceTest {

    private WarrantyClaimRepository claimRepository;
    private WarrantyClaimService warrantyClaimService;

    @BeforeEach
    void setUp() {
        claimRepository = mock(WarrantyClaimRepository.class);
        WarrantyItemRepository itemRepository = mock(WarrantyItemRepository.class);
        OrderGateway orderGateway = mock(OrderGateway.class);

        warrantyClaimService = new WarrantyClaimService(
                claimRepository,
                itemRepository,
                orderGateway,
                3
        );
    }

    @Test
    void debeAprobarReclamacionCuandoEstaPendienteYVigente() {
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(1L);
        claim.setStatus(WarrantyStatus.PENDIENTE);

        WarrantyItem item = new WarrantyItem(
                10L, "Laptop", 1, WarrantyReason.DEFECTUOSO, "Falla",
                LocalDateTime.now().minusDays(10), LocalDateTime.now().plusMonths(2)
        );
        claim.addItem(item);

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(WarrantyClaim.class))).thenAnswer(i -> i.getArgument(0));

        WarrantyDecisionRequest request = new WarrantyDecisionRequest(WarrantyStatus.APROBADO, null, "Todo correcto");
        WarrantyClaimStatusResponse respuesta = warrantyClaimService.resolveClaim(1L, request);

        assertEquals(WarrantyStatus.APROBADO, respuesta.status());
        verify(claimRepository, times(1)).save(claim);
    }

    @Test
    void debeRechazarReclamacionCuandoEstaPendiente() {
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(2L);
        claim.setStatus(WarrantyStatus.PENDIENTE);

        when(claimRepository.findById(2L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(WarrantyClaim.class))).thenAnswer(i -> i.getArgument(0));

        WarrantyDecisionRequest request = new WarrantyDecisionRequest(WarrantyStatus.RECHAZADO, null, "Daño por mal uso");
        WarrantyClaimStatusResponse respuesta = warrantyClaimService.resolveClaim(2L, request);

        assertEquals(WarrantyStatus.RECHAZADO, respuesta.status());
        verify(claimRepository, times(1)).save(claim);
    }

    @Test
    void debeLanzarExcepcionCuandoReclamacionNoExiste() {
        when(claimRepository.findById(999L)).thenReturn(Optional.empty());

        WarrantyDecisionRequest request = new WarrantyDecisionRequest(WarrantyStatus.APROBADO, null, null);

        assertThrows(
                WarrantyNotFoundException.class,
                () -> warrantyClaimService.resolveClaim(999L, request)
        );

        verify(claimRepository, never()).save(any());
    }

    @Test
    void debeLanzarExcepcionCuandoReclamacionYaFueResuelta() {
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(3L);
        claim.setStatus(WarrantyStatus.APROBADO);

        when(claimRepository.findById(3L)).thenReturn(Optional.of(claim));

        WarrantyDecisionRequest request = new WarrantyDecisionRequest(WarrantyStatus.RECHAZADO, null, null);

        assertThrows(
                WarrantyRuleException.class,
                () -> warrantyClaimService.resolveClaim(3L, request)
        );

        verify(claimRepository, never()).save(any());
    }

    @Test
    void debeLanzarExcepcionCuandoDecisionEsInvalida() {
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(4L);
        claim.setStatus(WarrantyStatus.PENDIENTE);

        when(claimRepository.findById(4L)).thenReturn(Optional.of(claim));

        WarrantyDecisionRequest requestVacio = new WarrantyDecisionRequest(null, null, null);

        assertThrows(
                WarrantyBadRequestException.class,
                () -> warrantyClaimService.resolveClaim(4L, requestVacio)
        );

        verify(claimRepository, never()).save(any());
    }

    @Test
    void debeLanzarExcepcionAlAprobarSiGarantiaEstaVencida() {
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(5L);
        claim.setStatus(WarrantyStatus.PENDIENTE);

        // Producto cuya vigencia expiró hace 5 días
        WarrantyItem itemExpirado = new WarrantyItem(
                10L, "Teclado", 1, WarrantyReason.DEFECTUOSO, "Falla",
                LocalDateTime.now().minusMonths(4), LocalDateTime.now().minusDays(5)
        );
        claim.addItem(itemExpirado);

        when(claimRepository.findById(5L)).thenReturn(Optional.of(claim));

        WarrantyDecisionRequest request = new WarrantyDecisionRequest(WarrantyStatus.APROBADO, null, "Aprobar");

        assertThrows(
                WarrantyRuleException.class,
                () -> warrantyClaimService.resolveClaim(5L, request)
        );

        verify(claimRepository, never()).save(any());
    }

    @Test
    void debeLanzarExcepcionCuandoLaSolicitudDeDecisionEsNula() {
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(6L);
        claim.setStatus(WarrantyStatus.PENDIENTE);
        when(claimRepository.findById(6L)).thenReturn(Optional.of(claim));

        assertThrows(WarrantyBadRequestException.class,
                () -> warrantyClaimService.resolveClaim(6L, null));
        verify(claimRepository, never()).save(any());
    }

    @Test
    void debeAceptarDecisionDesdeCampoStatusCuandoDecisionEsNula() {
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(7L);
        claim.setStatus(WarrantyStatus.PENDIENTE);
        when(claimRepository.findById(7L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(WarrantyClaim.class))).thenAnswer(i -> i.getArgument(0));

        WarrantyDecisionRequest request =
                new WarrantyDecisionRequest(null, WarrantyStatus.RECHAZADO, "Motivo alternativo");
        WarrantyClaimStatusResponse result = warrantyClaimService.resolveClaim(7L, request);

        assertEquals(WarrantyStatus.RECHAZADO, result.status());
        verify(claimRepository).save(claim);
    }

    @Test
    void debeCubrirLaConversiónDeEstadoDeReclamacionConFechaActualizada() {
        LocalDateTime updatedAt = LocalDateTime.now();
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(8L);
        claim.setStatus(WarrantyStatus.PENDIENTE);
        claim.setUpdatedAt(updatedAt);
        when(claimRepository.findById(8L)).thenReturn(Optional.of(claim));

        WarrantyClaimStatusResponse response = warrantyClaimService.getClaimStatus(8L);

        assertEquals(8L, response.id());
        assertEquals(WarrantyStatus.PENDIENTE, response.status());
        assertEquals(updatedAt, response.updatedAt());
    }


    @Test
    void debeRechazarDecisionPendientePorqueNoEsUnaResolucionFinal() {
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(10L);
        claim.setStatus(WarrantyStatus.PENDIENTE);
        when(claimRepository.findById(10L)).thenReturn(Optional.of(claim));
        WarrantyDecisionRequest request = new WarrantyDecisionRequest(WarrantyStatus.PENDIENTE, null, null);

        assertThrows(WarrantyBadRequestException.class,
                () -> warrantyClaimService.resolveClaim(10L, request));
        verify(claimRepository, never()).save(any());
    }

    @Test
    void debeAprobarReclamacionPendienteSinItemsVencidos() {
        WarrantyClaim claim = new WarrantyClaim(100L, 101L);
        claim.setId(11L);
        claim.setStatus(WarrantyStatus.PENDIENTE);
        when(claimRepository.findById(11L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(WarrantyClaim.class))).thenAnswer(i -> i.getArgument(0));

        WarrantyClaimStatusResponse response = warrantyClaimService.resolveClaim(
                11L, new WarrantyDecisionRequest(WarrantyStatus.APROBADO, null, null));

        assertEquals(WarrantyStatus.APROBADO, response.status());
        verify(claimRepository).save(claim);
    }
}