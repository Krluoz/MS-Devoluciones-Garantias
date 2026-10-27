package com.ecommerce.devoluciones.garantias.gateway;

import java.util.Optional;

public interface OrderGateway {

    boolean userExists(Long userId);

    Optional<OrderInfo> findOrder(Long orderId);
}