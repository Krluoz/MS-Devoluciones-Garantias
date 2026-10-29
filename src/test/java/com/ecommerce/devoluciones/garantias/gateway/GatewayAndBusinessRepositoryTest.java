package com.ecommerce.devoluciones.garantias.gateway;

import com.ecommerce.devoluciones.garantias.repository.ReturnBusinessRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GatewayAndBusinessRepositoryTest {

    @Test
    void stubGateway_reconoceSoloUsuariosConfigurados() {
        StubOrderGateway gateway = new StubOrderGateway();
        assertTrue(gateway.userExists(1L));
        assertTrue(gateway.userExists(2L));
        assertFalse(gateway.userExists(3L));
    }

    @Test
    void stubGateway_devuelvePedidosYProductosConfigurados() {
        StubOrderGateway gateway = new StubOrderGateway();
        OrderInfo order100 = gateway.findOrder(100L).orElseThrow();
        assertEquals(100L, order100.orderId());
        assertEquals(1L, order100.userId());
        assertNotNull(order100.purchaseDate());
        assertEquals(2, order100.items().size());
        assertEquals(25L, order100.items().get(0).productId());
        assertEquals("Teclado mecánico", order100.items().get(0).name());
        assertEquals(2, order100.items().get(0).quantity());

        assertEquals(1, gateway.findOrder(200L).orElseThrow().items().size());
        assertEquals(5, gateway.findOrder(300L).orElseThrow().items().get(0).quantity());
        assertEquals(2, gateway.findOrder(302L).orElseThrow().items().get(0).quantity());
        assertTrue(gateway.findOrder(999L).isEmpty());
    }

    @Test
    void returnBusinessRepository_validaPedidoContraUsuario() {
        ReturnBusinessRepository repository = new ReturnBusinessRepository();
        assertTrue(repository.pedidoValido(100L, 1L));
        assertTrue(repository.pedidoValido(200L, 2L));
        assertFalse(repository.pedidoValido(100L, 2L));
        assertFalse(repository.pedidoValido(999L, 1L));
    }

    @Test
    void returnBusinessRepository_devuelveProductoOColeccionVacia() {
        ReturnBusinessRepository repository = new ReturnBusinessRepository();
        List<?> product = repository.obtenerProductoDelPedido(100L, 25L);
        assertEquals("Teclado mecánico", product.get(0));
        assertEquals(2, product.get(1));
        assertEquals("Mouse inalámbrico", repository.obtenerProductoDelPedido(100L, 30L).get(0));
        assertEquals("Monitor 24 pulgadas", repository.obtenerProductoDelPedido(200L, 40L).get(0));
        assertTrue(repository.obtenerProductoDelPedido(999L, 1L).isEmpty());
        assertTrue(repository.obtenerProductoDelPedido(100L, 999L).isEmpty());
    }
}
