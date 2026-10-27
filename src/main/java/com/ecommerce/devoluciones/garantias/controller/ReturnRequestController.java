package com.ecommerce.devoluciones.garantias.controller;


import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.service.ReturnRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import com.ecommerce.devoluciones.garantias.config.UserIdResolver;
import com.ecommerce.devoluciones.garantias.dto.ReturnSummaryDTO;
import org.springframework.data.domain.Sort;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/returns")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;
    private final UserIdResolver userIdResolver;

    public ReturnRequestController(ReturnRequestService returnRequestService,UserIdResolver userIdResolver) {
        this.returnRequestService = returnRequestService;
        this.userIdResolver = userIdResolver;
    }

    //mapeo de endpoint crear un solicitud
    @PostMapping
    public ResponseEntity<ReturnRequest> crearSolicitud(
            @RequestBody ReturnRequest request) {

         ReturnRequest creada = returnRequestService.crearSolicitud(request);

         return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // mapeo de endpoint obtener una solicitus
    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequest> obtenerSolicitudPorId(
            @PathVariable Long id) {

        Optional<ReturnRequest> solicitud =
                returnRequestService.obtenerSolicitudPorId(id);

        return solicitud
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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
