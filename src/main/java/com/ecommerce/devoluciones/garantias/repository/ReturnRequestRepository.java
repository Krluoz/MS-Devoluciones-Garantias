package com.ecommerce.devoluciones.garantias.repository;

import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    Page<ReturnRequest> findByUserId(
            Long userId,
            Pageable pageable
    );

    @Query("""
           SELECT COALESCE(SUM(item.quantity), 0)
           FROM ReturnItem item
           WHERE item.returnRequest.orderId = :orderId
             AND item.itemId = :itemId
             AND item.returnRequest.status IN :statuses
           """)
    Long sumarCantidadSolicitada(
            @Param("orderId") Long orderId,
            @Param("itemId") Integer itemId,
            @Param("statuses") Collection<Status> statuses
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReturnRequest r WHERE r.id = :id")
    Optional<ReturnRequest> findByIdForUpdate(@Param("id") Long id);

}
