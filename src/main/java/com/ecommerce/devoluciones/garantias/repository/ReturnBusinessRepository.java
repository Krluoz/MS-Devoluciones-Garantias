package com.ecommerce.devoluciones.garantias.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ReturnBusinessRepository {

// Datos simulados para poder probar sin depender de otros microservicios.
// Pedido 100: pertenece al usuario 1 y contiene el producto 25,
// con nombre "Teclado mecánico" y cantidad comprada de 2 unidades.

    private final Map<Long, Long> pedidos = Map.of(
            100L, 1L,
            101L, 1L,
            200L, 2L,
            300L, 1L,
            301L, 2L,
            302L, 1L
    );

    private final Map<Long, Map<Long, List<Object>>> productos = Map.of(
            100L, Map.of(
                    25L, List.of("Teclado mecánico", 2),
                    30L, List.of("Mouse inalámbrico", 3)
            ),
            101L, Map.of(
                    25L, List.of("Teclado mecánico", 2)
            ),
            200L, Map.of(
                    40L, List.of("Monitor 24 pulgadas", 1)
            ),
            300L, Map.of(
                    25L, List.of("Teclado mecánico", 5)
            ),
            301L, Map.of(
                    30L, List.of("Mouse inalámbrico", 3)
            ),
            302L, Map.of(
                    40L, List.of("Monitor 24 pulgadas", 2)
            )
    );

    public boolean pedidoValido(Long orderId, Long userId) {
        return pedidos.containsKey(orderId)
                && pedidos.get(orderId).equals(userId);
    }

    public List<?> obtenerProductoDelPedido(Long orderId, Long productId) {
        return productos
                .getOrDefault(orderId, Map.of())
                .getOrDefault(productId, List.of());
    }
}

