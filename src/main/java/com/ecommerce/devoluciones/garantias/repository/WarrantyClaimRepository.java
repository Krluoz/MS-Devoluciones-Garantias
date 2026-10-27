package com.ecommerce.devoluciones.garantias.repository;

import com.ecommerce.devoluciones.garantias.model.WarrantyClaim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, Long> {
}