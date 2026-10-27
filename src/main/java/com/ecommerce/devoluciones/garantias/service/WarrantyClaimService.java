package com.ecommerce.devoluciones.garantias.service;

import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimRequest;
import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimResponse;
import com.ecommerce.devoluciones.garantias.dto.WarrantyItemRequest;
import com.ecommerce.devoluciones.garantias.exception.WarrantyBadRequestException;
import com.ecommerce.devoluciones.garantias.exception.WarrantyNotFoundException;
import com.ecommerce.devoluciones.garantias.exception.WarrantyRuleException;
import com.ecommerce.devoluciones.garantias.gateway.OrderGateway;
import com.ecommerce.devoluciones.garantias.gateway.OrderInfo;
import com.ecommerce.devoluciones.garantias.gateway.OrderItemInfo;
import com.ecommerce.devoluciones.garantias.model.WarrantyClaim;
import com.ecommerce.devoluciones.garantias.model.WarrantyItem;
import com.ecommerce.devoluciones.garantias.model.WarrantyStatus;
import com.ecommerce.devoluciones.garantias.repository.WarrantyClaimRepository;
import com.ecommerce.devoluciones.garantias.repository.WarrantyItemRepository;
import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WarrantyClaimService {

    // Estatus que "consumen" unidades de la compra
    private static final List<WarrantyStatus> ACTIVE_STATUSES =
            List.of(WarrantyStatus.PENDIENTE, WarrantyStatus.APROBADO);

    private final WarrantyClaimRepository claimRepository;
    private final WarrantyItemRepository itemRepository;
    private final OrderGateway orderGateway;
    private final int warrantyMonths;

    public WarrantyClaimService(WarrantyClaimRepository claimRepository,
                                WarrantyItemRepository itemRepository,
                                OrderGateway orderGateway,
                                @Value("${warranty.period-months}") int warrantyMonths) {
        this.claimRepository = claimRepository;
        this.itemRepository = itemRepository;
        this.orderGateway = orderGateway;
        this.warrantyMonths = warrantyMonths;
    }

    @Transactional
    public WarrantyClaimResponse createClaim(WarrantyClaimRequest request) {

        // 1. Producto repetido en la lista -> 400
        validateNoDuplicateProducts(request);

        // 2. El usuario existe -> 404
        if (!orderGateway.userExists(request.userId())) {
            throw new WarrantyNotFoundException("El usuario no existe.");
        }

        // 3. La orden existe y es de ese usuario -> 404
        OrderInfo order = orderGateway.findOrder(request.orderId())
                .filter(o -> o.userId().equals(request.userId()))
                .orElseThrow(() -> new WarrantyNotFoundException(
                        "La orden no existe o no pertenece al usuario."));

        // 4. La garantía sigue vigente -> 422
        LocalDateTime expiresAt = order.purchaseDate().plusMonths(warrantyMonths);
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new WarrantyRuleException(
                    "La garantía de esta orden venció el " + expiresAt.toLocalDate() + ".");
        }

        // 5. Validar cada producto y construir el reclamo
        WarrantyClaim claim = new WarrantyClaim(order.orderId(), order.userId());

        for (WarrantyItemRequest itemRequest : request.items()) {

            // 5a. El producto está en la orden -> 404
            OrderItemInfo product = order.items().stream()
                    .filter(i -> i.productId().equals(itemRequest.itemId()))
                    .findFirst()
                    .orElseThrow(() -> new WarrantyNotFoundException(
                            "El producto " + itemRequest.itemId()
                                    + " no pertenece a la orden indicada."));

            // 5b. La cantidad no excede lo que queda disponible -> 422
            long alreadyClaimed = itemRepository.sumClaimedQuantity(
                    order.orderId(), itemRequest.itemId(), ACTIVE_STATUSES);
            long available = product.quantity() - alreadyClaimed;

            if (itemRequest.quantity() > available) {
                throw new WarrantyRuleException(
                        "La cantidad solicitada (" + itemRequest.quantity()
                                + ") supera las unidades disponibles para garantía ("
                                + available + ") del producto " + itemRequest.itemId() + ".");
            }

            // El nombre y las fechas salen del servidor, no del cliente
            claim.addItem(new WarrantyItem(
                    product.productId(),
                    product.name(),
                    itemRequest.quantity(),
                    itemRequest.reason(),
                    itemRequest.description(),
                    order.purchaseDate(),
                    expiresAt));
        }

        // 6. Guardar (cascade guarda también los items)
        return WarrantyClaimResponse.from(claimRepository.save(claim));
    }

    //Método GET claim por ID
    @Transactional(readOnly = true)
    public WarrantyClaimStatusResponse getClaimStatus(Long claimId) {
        WarrantyClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new WarrantyNotFoundException(
                        "La reclamación de garantía " + claimId + " no existe."));

        return WarrantyClaimStatusResponse.from(claim);
    }

    private void validateNoDuplicateProducts(WarrantyClaimRequest request) {
        Set<Long> seen = new HashSet<>();
        for (WarrantyItemRequest item : request.items()) {
            if (!seen.add(item.itemId())) {
                throw new WarrantyBadRequestException(
                        "El producto " + item.itemId() + " está repetido en la solicitud.");
            }
        }
    }
}