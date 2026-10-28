package com.ecommerce.devoluciones.garantias.service;

import com.ecommerce.devoluciones.garantias.gateway.OrderGateway;
import com.ecommerce.devoluciones.garantias.model.ReturnItem;
import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.model.Status;
import com.ecommerce.devoluciones.garantias.repository.ReturnBusinessRepository;
import com.ecommerce.devoluciones.garantias.repository.ReturnRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.ecommerce.devoluciones.garantias.dto.ReturnSummaryDTO;

@Service
public class ReturnRequestService {
    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnBusinessRepository returnBusinessRepository;
    private final OrderGateway orderGateway;

    private static final long PLAZO_DEVOLUCION_DIAS = 30;


    public ReturnRequestService(ReturnRequestRepository returnRequestRepository, ReturnBusinessRepository returnBusinessRepository, OrderGateway orderGateway) {
        this.returnRequestRepository = returnRequestRepository;
        this.returnBusinessRepository = returnBusinessRepository;
        this.orderGateway = orderGateway;
    }

    // Creamos una solicitud de devolución
    @Transactional
    public ReturnRequest crearSolicitud(ReturnRequest request) {

        //Valida que el pedido exista, pertenezca al usuario y no esté cancelado.
        var pedido = orderGateway.findOrder(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El pedido no existe."
                ));

        if (!pedido.userId().equals(request.getUserId())) {
            throw new IllegalArgumentException(
                    "El pedido no pertenece al usuario indicado."
            );
        }

        //Valida que la fecha de compra exista.
        if (pedido.purchaseDate() == null) {
            throw new IllegalArgumentException(
                    "No se puede validar el plazo de devolución porque el pedido no tiene fecha de compra."
            );
        }

        //Valida el plazo máximo de 30 días naturales.
        LocalDateTime fechaLimite = pedido.purchaseDate()
                .plusDays(PLAZO_DEVOLUCION_DIAS);

        if (LocalDateTime.now().isAfter(fechaLimite)) {
            throw new IllegalArgumentException(
                    "No se puede solicitar la devolución porque han transcurrido más de "
                            + PLAZO_DEVOLUCION_DIAS
                            + " días desde la compra."
            );
        }

        //Conserva la validación existente del pedido.
        if (!returnBusinessRepository.pedidoValido(
                request.getOrderId(),
                request.getUserId())) {

            throw new IllegalArgumentException(
                    "El pedido no existe, no pertenece al usuario o está cancelado."
            );
        }

        //Valida que la solicitud contenga artículos.
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException(
                    "La solicitud debe contener al menos un artículo."
            );
        }

        //Estados que reservan unidades.
        List<Status> estadosActivos = List.of(
                Status.PENDIENTE,
                Status.RECIBIDO,
                Status.APROBADO
        );

        // Agrupa las cantidades solicitadas por artículo. Esto impide superar el límite enviando el mismo artículo
        // varias veces dentro de una sola solicitud.
        Map<Integer, Integer> cantidadesPorArticulo =
                request.getItems().stream()
                        .collect(Collectors.groupingBy(
                                ReturnItem::getItemId,
                                Collectors.summingInt(ReturnItem::getQuantity)
                        ));

        //Valida cada artículo y su cantidad acumulada.
        for (Map.Entry<Integer, Integer> entrada
                : cantidadesPorArticulo.entrySet()) {

            Integer itemId = entrada.getKey();

        //Consultar las unidades ya solicitadas para este producto en solicitudes pendientes, recibidas o aprobadas.
            Long cantidadYaSolicitada =
                    returnRequestRepository.sumarCantidadSolicitada(
                            request.getOrderId(),
                            itemId,
                            estadosActivos
                    );

            if (cantidadYaSolicitada > 0) {
                throw new IllegalArgumentException(
                        "Ya existe una solicitud de devolución activa para el artículo "
                                + itemId
                                + " en el pedido "
                                + request.getOrderId()
                                + ". No se puede crear otra solicitud para el mismo artículo."
                );
            }

            int cantidadSolicitada = entrada.getValue();

            var producto = returnBusinessRepository.obtenerProductoDelPedido(
                    request.getOrderId(),
                    itemId.longValue()
            );

            //Verifica que el artículo pertenezca al pedido.
            if (producto.isEmpty()) {
                throw new IllegalArgumentException(
                        "El artículo con ID " + itemId
                                + " no pertenece al pedido indicado."
                );
            }

            String nombreProducto = (String) producto.get(0);
            int cantidadComprada = ((Number) producto.get(1)).intValue();

            //Calcula las unidades que todavía pueden devolverse.
            long cantidadDisponible =
                    cantidadComprada - cantidadYaSolicitada;

            if (cantidadSolicitada > cantidadDisponible) {
                throw new IllegalArgumentException(
                        "La cantidad solicitada para el artículo "
                                + nombreProducto
                                + " supera las unidades disponibles para devolución. "
                                + "Compradas: " + cantidadComprada
                                + ", ya solicitadas: " + cantidadYaSolicitada
                                + ", disponibles: " + Math.max(0, cantidadDisponible)
                                + "."
                );
            }

            //Asigna el nombre a todas las entradas de ese artículo.
            for (ReturnItem item : request.getItems()) {
                if (itemId.equals(item.getItemId())) {
                    item.setItemName(nombreProducto);
                }
            }
        }

        //Asocia todos los artículos con la solicitud.
        for (ReturnItem item : request.getItems()) {
            item.setReturnRequest(request);
        }

        //Crear la solicitud con estado inicial pendiente.
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
}






