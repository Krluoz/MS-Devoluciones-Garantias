package com.ecommerce.devoluciones.garantias.service;

import com.ecommerce.devoluciones.garantias.model.ReturnItem;
import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.model.Status;
import com.ecommerce.devoluciones.garantias.repository.ReturnBusinessRepository;
import com.ecommerce.devoluciones.garantias.repository.ReturnRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
}





