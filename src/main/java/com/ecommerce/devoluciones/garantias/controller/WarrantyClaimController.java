package com.ecommerce.devoluciones.garantias.controller;

import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimRequest;
import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimResponse;
import com.ecommerce.devoluciones.garantias.service.WarrantyClaimService;
import com.ecommerce.devoluciones.garantias.dto.WarrantyClaimStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import com.ecommerce.devoluciones.garantias.dto.WarrantyDecisionRequest;

@RestController
@RequestMapping("/api/warranties/claims")
public class WarrantyClaimController {

    private final WarrantyClaimService claimService;

    public WarrantyClaimController(WarrantyClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<WarrantyClaimResponse> createClaim(
            @Valid @RequestBody WarrantyClaimRequest request) {

        WarrantyClaimResponse created = claimService.createClaim(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    //Método GET para obtener claims por ID
    @GetMapping("/{id}")
    public ResponseEntity<WarrantyClaimStatusResponse> getClaimStatus(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(claimService.getClaimStatus(id));
    }
    @PatchMapping({"/admin/{id}/decision", "/{id}/decision"})
    public ResponseEntity<WarrantyClaimStatusResponse> resolveClaim(
            @PathVariable("id") Long id,
            @RequestBody WarrantyDecisionRequest request) {

        WarrantyClaimStatusResponse response = claimService.resolveClaim(id, request);
        return ResponseEntity.ok(response);
    }
}