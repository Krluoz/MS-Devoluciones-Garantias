package com.ecommerce.devoluciones.garantias.service;

import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimRequest;
import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimResponse;
import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimStatusResponse;
import com.ecommerce.devoluciones.garantias.dto.WarrantyItemRequest;
import com.ecommerce.devoluciones.garantias.exception.WarrantyBadRequestException;
import com.ecommerce.devoluciones.garantias.exception.WarrantyNotFoundException;
import com.ecommerce.devoluciones.garantias.exception.WarrantyRuleException;
import com.ecommerce.devoluciones.garantias.gateway.OrderGateway;
import com.ecommerce.devoluciones.garantias.gateway.OrderInfo;
import com.ecommerce.devoluciones.garantias.gateway.OrderItemInfo;
import com.ecommerce.devoluciones.garantias.model.WarrantyClaim;
import com.ecommerce.devoluciones.garantias.model.WarrantyReason;
import com.ecommerce.devoluciones.garantias.model.WarrantyStatus;
import com.ecommerce.devoluciones.garantias.repository.WarrantyClaimRepository;
import com.ecommerce.devoluciones.garantias.repository.WarrantyItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarrantyClaimCreateServiceTest {

    @Mock
    private WarrantyClaimRepository claimRepository;

    @Mock
    private WarrantyItemRepository itemRepository;

    @Mock
    private OrderGateway orderGateway;

    private WarrantyClaimService service;

    @BeforeEach
    void setUp() {
        // Plazo de garantía de 3 meses, como en application.properties
        service = new WarrantyClaimService(claimRepository, itemRepository, orderGateway, 3);
    }

    // ---------- helpers ----------

    // Orden 100 del usuario 1: compró 2 unidades del producto 25
    private OrderInfo orderBoughtMonthsAgo(int months) {
        return new OrderInfo(100L, 1L, LocalDateTime.now().minusMonths(months),
                List.of(new OrderItemInfo(25L, "Teclado mecánico", 2)));
    }

    // Request del usuario 1 sobre la orden 100 con un solo producto
    private WarrantyClaimRequest requestFor(Long itemId, int quantity) {
        return new WarrantyClaimRequest(1L, 100L, List.of(
                new WarrantyItemRequest(itemId, quantity, WarrantyReason.DEFECTUOSO, "No funciona")));
    }

    // =====================================================
    // createClaim: casos exitosos
    // =====================================================

    @Test
    void createClaim_validRequest_savesClaimAsPendiente() {
        when(orderGateway.userExists(1L)).thenReturn(true);
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(orderBoughtMonthsAgo(1)));
        when(itemRepository.sumClaimedQuantity(eq(100L), eq(25L), anyCollection())).thenReturn(0L);
        when(claimRepository.save(any(WarrantyClaim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WarrantyClaimResponse response = service.createClaim(requestFor(25L, 2));

        assertThat(response.status()).isEqualTo(WarrantyStatus.PENDIENTE);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.orderId()).isEqualTo(100L);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).itemName()).isEqualTo("Teclado mecánico");
        assertThat(response.items().get(0).reason()).isEqualTo(WarrantyReason.DEFECTUOSO);
        assertThat(response.items().get(0).description()).isEqualTo("No funciona");
        assertThat(response.items().get(0).purchaseDate()).isNotNull();
        assertThat(response.items().get(0).warrantyExpiresAt()).isAfter(response.items().get(0).purchaseDate());
        verify(claimRepository).save(any(WarrantyClaim.class));
    }

    @Test
    void createClaim_quantityEqualsRemainingUnits_savesClaim() {
        when(orderGateway.userExists(1L)).thenReturn(true);
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(orderBoughtMonthsAgo(1)));
        // Compró 2, ya hay 1 reclamada -> pedir 1 (justo lo que queda) debe pasar
        when(itemRepository.sumClaimedQuantity(eq(100L), eq(25L), anyCollection())).thenReturn(1L);
        when(claimRepository.save(any(WarrantyClaim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WarrantyClaimResponse response = service.createClaim(requestFor(25L, 1));

        assertThat(response.items().get(0).quantity()).isEqualTo(1);
        verify(claimRepository).save(any(WarrantyClaim.class));
    }

    // =====================================================
    // createClaim: validaciones que fallan
    // =====================================================

    @Test
    void createClaim_duplicateProductInList_throwsBadRequest() {
        WarrantyClaimRequest request = new WarrantyClaimRequest(1L, 100L, List.of(
                new WarrantyItemRequest(25L, 1, WarrantyReason.DEFECTUOSO, "Falla"),
                new WarrantyItemRequest(25L, 1, WarrantyReason.FALLA_FUNCIONAL, "Otra falla")
        ));

        assertThatThrownBy(() -> service.createClaim(request))
                .isInstanceOf(WarrantyBadRequestException.class)
                .hasMessageContaining("repetido");

        // Falla antes de consultar a otros servicios o guardar algo
        verifyNoInteractions(orderGateway);
        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_userDoesNotExist_throwsNotFound() {
        when(orderGateway.userExists(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.createClaim(requestFor(25L, 1)))
                .isInstanceOf(WarrantyNotFoundException.class)
                .hasMessageContaining("usuario");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_orderDoesNotExist_throwsNotFound() {
        when(orderGateway.userExists(1L)).thenReturn(true);
        when(orderGateway.findOrder(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createClaim(requestFor(25L, 1)))
                .isInstanceOf(WarrantyNotFoundException.class)
                .hasMessageContaining("orden");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_orderNotFromUser_throwsNotFound() {
        // El usuario 2 pide garantía de la orden 100, que es del usuario 1
        WarrantyClaimRequest request = new WarrantyClaimRequest(2L, 100L, List.of(
                new WarrantyItemRequest(25L, 1, WarrantyReason.DEFECTUOSO, "Falla")
        ));
        when(orderGateway.userExists(2L)).thenReturn(true);
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(orderBoughtMonthsAgo(1)));

        assertThatThrownBy(() -> service.createClaim(request))
                .isInstanceOf(WarrantyNotFoundException.class)
                .hasMessageContaining("orden");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_expiredWarranty_throwsRuleException() {
        when(orderGateway.userExists(1L)).thenReturn(true);
        // Comprada hace 5 meses, el plazo es de 3
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(orderBoughtMonthsAgo(5)));

        assertThatThrownBy(() -> service.createClaim(requestFor(25L, 1)))
                .isInstanceOf(WarrantyRuleException.class)
                .hasMessageContaining("venció");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_productNotInOrder_throwsNotFound() {
        when(orderGateway.userExists(1L)).thenReturn(true);
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(orderBoughtMonthsAgo(1)));

        // El producto 40 no está en la orden 100
        assertThatThrownBy(() -> service.createClaim(requestFor(40L, 1)))
                .isInstanceOf(WarrantyNotFoundException.class)
                .hasMessageContaining("40");

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_quantityGreaterThanPurchased_throwsRuleException() {
        when(orderGateway.userExists(1L)).thenReturn(true);
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(orderBoughtMonthsAgo(1)));
        // Compró 2 y no hay reclamos previos; pide 3
        when(itemRepository.sumClaimedQuantity(eq(100L), eq(25L), anyCollection())).thenReturn(0L);

        assertThatThrownBy(() -> service.createClaim(requestFor(25L, 3)))
                .isInstanceOf(WarrantyRuleException.class);

        verify(claimRepository, never()).save(any());
    }

    @Test
    void createClaim_quantityExceedsRemainingUnits_throwsRuleException() {
        when(orderGateway.userExists(1L)).thenReturn(true);
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(orderBoughtMonthsAgo(1)));
        // Compró 2, ya hay 1 en otro reclamo -> solo queda 1; pide 2
        when(itemRepository.sumClaimedQuantity(eq(100L), eq(25L), anyCollection())).thenReturn(1L);

        assertThatThrownBy(() -> service.createClaim(requestFor(25L, 2)))
                .isInstanceOf(WarrantyRuleException.class);

        verify(claimRepository, never()).save(any());
    }

    // =====================================================
    // getClaimStatus
    // =====================================================

    @Test
    void getClaimStatus_existingClaim_returnsStatus() {
        WarrantyClaim claim = new WarrantyClaim(100L, 1L);
        claim.setId(1L);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        WarrantyClaimStatusResponse response = service.getClaimStatus(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(WarrantyStatus.PENDIENTE);
    }

    @Test
    void getClaimStatus_claimDoesNotExist_throwsNotFound() {
        when(claimRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getClaimStatus(999L))
                .isInstanceOf(WarrantyNotFoundException.class);
    }
}