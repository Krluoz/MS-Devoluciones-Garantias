package com.ecommerce.devoluciones.garantias.repository;

import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Page<ReturnRequest> findByUserId(
            Long userId,
            Pageable pageable
    );
}
