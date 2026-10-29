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
import com.ecommerce.devoluciones.garantias.security.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/warranties/claims")
public class WarrantyClaimController {

    private final WarrantyClaimService claimService;

    public WarrantyClaimController(WarrantyClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<WarrantyClaimResponse> createClaim(
            @Valid @RequestBody WarrantyClaimRequest request,
            Authentication authentication) {

        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        WarrantyClaimResponse created = claimService.createClaim(user.id(), request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }
    //Método GET para obtener claims por ID
    @GetMapping("/{id}")
    public ResponseEntity<WarrantyClaimStatusResponse> getClaimStatus(
            @PathVariable("id") Long id, Authentication authentication) {

        AuthenticatedUser user = AuthenticatedUser.from(authentication);
        return ResponseEntity.ok(claimService.getClaimStatus(id, user));
    }
    @PatchMapping({"/admin/{id}/decision", "/{id}/decision"})
    public ResponseEntity<WarrantyClaimStatusResponse> resolveClaim(
            @PathVariable("id") Long id,
            @RequestBody WarrantyDecisionRequest request) {

        WarrantyClaimStatusResponse response = claimService.resolveClaim(id, request);
        return ResponseEntity.ok(response);
    }
}