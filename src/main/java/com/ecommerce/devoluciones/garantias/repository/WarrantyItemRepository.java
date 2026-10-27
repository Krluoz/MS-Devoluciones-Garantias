package com.ecommerce.devoluciones.garantias.repository;

import com.ecommerce.devoluciones.garantias.model.WarrantyItem;
import com.ecommerce.devoluciones.garantias.model.WarrantyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface WarrantyItemRepository extends JpaRepository<WarrantyItem, Long> {

    // Suma las unidades de un producto, en una orden, que ya están en reclamos
    // con alguno de los estatus indicados. Si no hay ninguno, devuelve 0.
    @Query("""
            SELECT COALESCE(SUM(i.quantity), 0L)
            FROM WarrantyItem i
            WHERE i.claim.orderId = :orderId
              AND i.itemId = :itemId
              AND i.claim.status IN :statuses
            """)
    Long sumClaimedQuantity(@Param("orderId") Long orderId,
                            @Param("itemId") Long itemId,
                            @Param("statuses") Collection<WarrantyStatus> statuses);
}