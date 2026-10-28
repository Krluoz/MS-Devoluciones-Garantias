package com.ecommerce.devoluciones.garantias.controller;


import com.ecommerce.devoluciones.garantias.dto.ReturnItemResponseDto;
import com.ecommerce.devoluciones.garantias.dto.ReturnRequestCreateDto;
import com.ecommerce.devoluciones.garantias.dto.ReturnRequestResponseDto;
import com.ecommerce.devoluciones.garantias.model.ReturnItem;
import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.service.ReturnRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import com.ecommerce.devoluciones.garantias.config.UserIdResolver;
import com.ecommerce.devoluciones.garantias.dto.ReturnSummaryDTO;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("api/returns")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;
    private final UserIdResolver userIdResolver;

    public ReturnRequestController(ReturnRequestService returnRequestService,UserIdResolver userIdResolver) {
        this.returnRequestService = returnRequestService;
        this.userIdResolver = userIdResolver;
    }

    //mapeo de endpoint para crear una solicitud
    @PostMapping
    public ResponseEntity<ReturnRequestResponseDto> crearSolicitud(
            @Valid @RequestBody ReturnRequestCreateDto requestDto) {

        ReturnRequest request = new ReturnRequest();

        request.setOrderId(requestDto.orderId());
        request.setUserId(requestDto.userId());
        request.setItems(requestDto.items().stream().map(
                itemDto-> {
                    ReturnItem item = new ReturnItem();
                    item.setItemId(itemDto.itemId());
                    item.setQuantity(itemDto.quantity());
                    item.setReason(itemDto.reason());

                    return item;
                }).toList()
        );

        ReturnRequest creada = returnRequestService.crearSolicitud(request);

        ReturnRequestResponseDto respuesta = convertirAResponseDto(creada);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    // mapeo de endpoint consultar una solicitud por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerSolicitudPorId(
            @PathVariable
            @Positive(message = "El ID de la solicitud no es válido.") Long id) {

        Optional<ReturnRequest> solicitud = returnRequestService.obtenerSolicitudPorId(id);

        if (solicitud.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error",
                            "No se encontro la solicitud de devolucion con ID " + id + "."));
        }

        return ResponseEntity.ok(convertirAResponseDto(solicitud.get()));
    }

    // Convertir la entidad a un DTO de respuesta
    private ReturnRequestResponseDto convertirAResponseDto(
            ReturnRequest request) {

        return new ReturnRequestResponseDto(
                request.getId(),
                request.getOrderId(),
                request.getUserId(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getItems() == null
                        ? java.util.List.of()
                        : request.getItems().stream()
                        .map(this::convertirItemAResponseDto)
                        .toList()
        );
    }

    // Convertir cada artículo a su DTO de respuesta
    private ReturnItemResponseDto convertirItemAResponseDto(
            ReturnItem item) {

        return new ReturnItemResponseDto(
                item.getId(),
                item.getItemId(),
                item.getItemName(),
                item.getQuantity(),
                item.getReason()
        );
    }


    // Consultar las devoluciones del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<Page<ReturnSummaryDTO>> obtenerMisDevoluciones(
            Authentication authentication,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        // 1. Obtener el ID del usuario autenticado
        Long userId = userIdResolver.obtenerUserId(authentication);

        // 2. Consultar sus devoluciones
        Page<ReturnSummaryDTO> solicitudes =
                returnRequestService.obtenerSolicitudesPorUsuario(
                        userId, pageable);

        // 3. Devolver la respuesta HTTP 200
        return ResponseEntity.ok(solicitudes);

    }


}