package com.ecommerce.devoluciones.garantias.controller;


import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.service.ReturnRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/returns")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;

    public ReturnRequestController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
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
}
