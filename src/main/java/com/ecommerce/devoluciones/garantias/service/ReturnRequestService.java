package com.ecommerce.devoluciones.garantias.service;

import com.ecommerce.devoluciones.garantias.model.ReturnItem;
import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.model.Status;
import com.ecommerce.devoluciones.garantias.repository.ReturnBusinessRepository;
import com.ecommerce.devoluciones.garantias.repository.ReturnRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ecommerce.devoluciones.garantias.dto.ReturnSummaryDTO;

@Service
public class ReturnRequestService {
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnBusinessRepository returnBusinessRepository;

    public ReturnRequestService(ReturnRequestRepository returnRequestRepository, ReturnBusinessRepository returnBusinessRepository) {
        this.returnRequestRepository = returnRequestRepository;
        this.returnBusinessRepository = returnBusinessRepository;
    }

    // Creamos una solicitud de devolución
    @Transactional
    public ReturnRequest crearSolicitud(ReturnRequest request) {

        // Validar que el pedido exista, pertenezca al usuario
        // y no esté cancelado.
        if (!returnBusinessRepository.pedidoValido(
                request.getOrderId(),
                request.getUserId())) {
            throw new IllegalArgumentException(
                    "El pedido no existe, no pertenece al usuario o está cancelado.");
        }

        // Validar cada artículo antes de guardar.
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "La solicitud debe contener al menos un artículo.");
        }

        for (ReturnItem item : request.getItems()) {

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero.");
            }

            var producto = returnBusinessRepository.obtenerProductoDelPedido(
                    request.getOrderId(),
                    item.getItemId().longValue());

            if (producto.isEmpty()) {
                throw new IllegalArgumentException(
                        "El artículo no pertenece al pedido indicado.");
            }

            item.setItemName((String) producto.get(0));

            int cantidadComprada =
                    ((Number) producto.get(1)).intValue();;

            if (item.getQuantity() > cantidadComprada) {
                throw new IllegalArgumentException(
                        "La cantidad solicitada supera la cantidad comprada.");
            }

            item.setReturnRequest(request);
        }

        request.setStatus(Status.PENDIENTE);

        return returnRequestRepository.save(request);
    }

    public Optional<ReturnRequest> obtenerSolicitudPorId(Long id) {
        return returnRequestRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<ReturnSummaryDTO> obtenerSolicitudesPorUsuario(
            Long userId,
            Pageable pageable) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del usuario no es válido.");
        }

        return returnRequestRepository
                .findByUserId(userId, pageable)
                .map(request -> new ReturnSummaryDTO(
                        request.getId(),
                        request.getOrderId(),
                        request.getStatus(),
                        request.getCreatedAt(),
                        request.getUpdatedAt()
                ));
    }
    @Transactional
    public ReturnRequest registrarRecepcion(Long id) {
        // 1. Buscamos la devolución por su ID (reutilizando el repositorio de Carlos)
        ReturnRequest request = returnRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la solicitud de devolución con ID " + id));
        // 2. Regla de negocio del PDF: solo avanza si ya fue aprobada
        if (request.getStatus() != Status.APROBADO) {
            throw new IllegalArgumentException(
                    "No se puede registrar la recepción. La devolución debe estar en estado APROBADO, pero actualmente está en: " + request.getStatus());
        }
        // 3. Cambiamos el estado a RECIBIDO
        request.setStatus(Status.RECIBIDO);
        // 4. Guardamos en MySQL y retornamos el resultado
        return returnRequestRepository.save(request);

    }





}
