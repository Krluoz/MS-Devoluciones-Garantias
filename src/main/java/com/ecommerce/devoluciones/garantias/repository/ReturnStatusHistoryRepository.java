package com.ecommerce.devoluciones.garantias.repository;

import com.ecommerce.devoluciones.garantias.model.ReturnStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnStatusHistoryRepository
        extends JpaRepository<ReturnStatusHistory, Long> {
}
