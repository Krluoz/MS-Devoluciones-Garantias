package com.ecommerce.devoluciones.garantias.dto;

import com.ecommerce.devoluciones.garantias.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoCoverageTest {

    @Test
    void returnRequestDtosExponenSusCampos() {
        LocalDateTime now = LocalDateTime.now();
        ReturnItemRequestDto itemRequest = new ReturnItemRequestDto(4, 2, ReturnReason.ITEM_DEFECTUOSO);
        assertEquals(4, itemRequest.itemId());
        assertEquals(2, itemRequest.quantity());
        assertEquals(ReturnReason.ITEM_DEFECTUOSO, itemRequest.reason());

        ReturnRequestCreateDto create = new ReturnRequestCreateDto(10L, List.of(itemRequest));
        assertEquals(10L, create.orderId());
        assertEquals(List.of(itemRequest), create.items());

        ReturnItemResponseDto itemResponse = new ReturnItemResponseDto(
                1L, 4, "Teclado", 2, ReturnReason.ITEM_DEFECTUOSO);
        assertEquals(1L, itemResponse.id());
        assertEquals(4, itemResponse.itemId());
        assertEquals("Teclado", itemResponse.itemName());
        assertEquals(2, itemResponse.quantity());
        assertEquals(ReturnReason.ITEM_DEFECTUOSO, itemResponse.reason());

        ReturnRequestResponseDto response = new ReturnRequestResponseDto(
                8L, 10L, 20L, Status.PENDIENTE, now, now, List.of(itemResponse));
        assertEquals(8L, response.id());
        assertEquals(10L, response.orderId());
        assertEquals(20L, response.userId());
        assertEquals(Status.PENDIENTE, response.status());
        assertEquals(now, response.createdAt());
        assertEquals(now, response.updatedAt());
        assertEquals(List.of(itemResponse), response.items());

        ReturnSummaryDTO summary = new ReturnSummaryDTO(8L, 10L, Status.PENDIENTE, now, now);
        assertEquals(8L, summary.id());
        assertEquals(10L, summary.orderId());
        assertEquals(Status.PENDIENTE, summary.status());
        assertEquals(now, summary.createdAt());
        assertEquals(now, summary.updatedAt());

        ReturnDecisionRequest decision = new ReturnDecisionRequest(Status.APROBADO);
        assertEquals(Status.APROBADO, decision.decision());
    }

    @Test
    void warrantyDtosExponeCamposYConvierteEntidades() {
        LocalDateTime purchase = LocalDateTime.now().minusDays(3);
        LocalDateTime expires = purchase.plusMonths(3);
        WarrantyItemRequest itemRequest = new WarrantyItemRequest(
                25L, 1, WarrantyReason.DEFECTUOSO, "No enciende");
        assertEquals(25L, itemRequest.itemId());
        assertEquals(1, itemRequest.quantity());
        assertEquals(WarrantyReason.DEFECTUOSO, itemRequest.reason());
        assertEquals("No enciende", itemRequest.description());

        WarrantyClaimRequest claimRequest = new WarrantyClaimRequest(100L, List.of(itemRequest));
        assertEquals(100L, claimRequest.orderId());
        assertEquals(List.of(itemRequest), claimRequest.items());

        WarrantyClaim claim = new WarrantyClaim(100L, 1L);
        claim.setId(7L);
        claim.setCreatedAt(purchase);
        claim.setUpdatedAt(expires);
        WarrantyItem item = new WarrantyItem(25L, "Teclado", 1,
                WarrantyReason.DEFECTUOSO, "No enciende", purchase, expires);
        item.setId(3L);
        claim.addItem(item);

        WarrantyItemResponse itemResponse = WarrantyItemResponse.from(item);
        assertEquals(3L, itemResponse.id());
        assertEquals(25L, itemResponse.itemId());
        assertEquals("Teclado", itemResponse.itemName());
        assertEquals(1, itemResponse.quantity());
        assertEquals(WarrantyReason.DEFECTUOSO, itemResponse.reason());
        assertEquals("No enciende", itemResponse.description());
        assertEquals(purchase, itemResponse.purchaseDate());
        assertEquals(expires, itemResponse.warrantyExpiresAt());

        WarrantyClaimResponse claimResponse = WarrantyClaimResponse.from(claim);
        assertEquals(7L, claimResponse.id());
        assertEquals(100L, claimResponse.orderId());
        assertEquals(1L, claimResponse.userId());
        assertEquals(WarrantyStatus.PENDIENTE, claimResponse.status());
        assertEquals(purchase, claimResponse.createdAt());
        assertEquals(1, claimResponse.items().size());

        WarrantyClaimStatusResponse status = WarrantyClaimStatusResponse.from(claim);
        assertEquals(7L, status.id());
        assertEquals(WarrantyStatus.PENDIENTE, status.status());
        assertEquals(expires, status.updatedAt());

        WarrantyDecisionRequest decision = new WarrantyDecisionRequest(
                WarrantyStatus.APROBADO, WarrantyStatus.RECHAZADO, "evaluado");
        assertEquals(WarrantyStatus.APROBADO, decision.decision());
        assertEquals(WarrantyStatus.RECHAZADO, decision.status());
        assertEquals("evaluado", decision.reason());
        assertEquals(WarrantyStatus.APROBADO, decision.getEffectiveDecision());
        assertEquals(WarrantyStatus.RECHAZADO,
                new WarrantyDecisionRequest(null, WarrantyStatus.RECHAZADO, null).getEffectiveDecision());
    }
}
