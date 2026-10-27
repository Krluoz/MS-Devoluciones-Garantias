package com.ecommerce.devoluciones.garantias.gateway;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// TEMPORAL: datos de prueba mientras no existan los microservicios reales.
// Cuando haya una implementación real de OrderGateway, borrar esta clase.
@Component
public class StubOrderGateway implements OrderGateway {

    private static final Set<Long> USERS = Set.of(1L, 2L);

    @Override
    public boolean userExists(Long userId) {
        return USERS.contains(userId);
    }

    @Override
    public Optional<OrderInfo> findOrder(Long orderId) {
        LocalDateTime now = LocalDateTime.now();

        Map<Long, OrderInfo> orders = Map.of(
                100L, new OrderInfo(100L, 1L, now.minusMonths(1), List.of(
                        new OrderItemInfo(25L, "Teclado mecánico", 2),
                        new OrderItemInfo(30L, "Mouse inalámbrico", 3))),
                101L, new OrderInfo(101L, 1L, now.minusMonths(5), List.of(
                        new OrderItemInfo(25L, "Teclado mecánico", 2))),
                200L, new OrderInfo(200L, 2L, now.minusDays(10), List.of(
                        new OrderItemInfo(40L, "Monitor 24 pulgadas", 1)))
        );

        return Optional.ofNullable(orders.get(orderId));
    }
}