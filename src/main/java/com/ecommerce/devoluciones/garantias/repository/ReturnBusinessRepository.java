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
            100L, 1L
    );

    private final Map<Long, Map<Long, List<Object>>> productos = Map.of(
            100L, Map.of(
                        25L, List.of("Teclado mecánico", 3)
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

