package com.ecommerce.devoluciones.garantias.gateway;

import java.time.LocalDateTime;
import java.util.List;

public record OrderInfo(
        Long orderId,
        Long userId,
        LocalDateTime purchaseDate,
        List<OrderItemInfo> items
) {
}