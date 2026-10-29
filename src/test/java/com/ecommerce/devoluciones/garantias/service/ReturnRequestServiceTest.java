package com.ecommerce.devoluciones.garantias.service;

import com.ecommerce.devoluciones.garantias.dto.ReturnSummaryDTO;
import com.ecommerce.devoluciones.garantias.exception.DuplicateReturnRequestException;
import com.ecommerce.devoluciones.garantias.gateway.OrderGateway;
import com.ecommerce.devoluciones.garantias.gateway.OrderInfo;
import com.ecommerce.devoluciones.garantias.model.ReturnItem;
import com.ecommerce.devoluciones.garantias.model.ReturnReason;
import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.model.ReturnStatusHistory;
import com.ecommerce.devoluciones.garantias.model.Status;
import com.ecommerce.devoluciones.garantias.repository.ReturnBusinessRepository;
import com.ecommerce.devoluciones.garantias.repository.ReturnRequestRepository;
import com.ecommerce.devoluciones.garantias.repository.ReturnStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReturnRequestServiceTest {

    private ReturnRequestRepository returnRequestRepository;
    private ReturnBusinessRepository returnBusinessRepository;
    private OrderGateway orderGateway;
    private ReturnStatusHistoryRepository returnStatusHistoryRepository;
    private ReturnRequestService service;

    @BeforeEach
    void setUp() {

        returnRequestRepository = mock(ReturnRequestRepository.class);
        returnBusinessRepository = mock(ReturnBusinessRepository.class);
        orderGateway = mock(OrderGateway.class);
        returnStatusHistoryRepository = mock(ReturnStatusHistoryRepository.class);
        service = new ReturnRequestService(returnRequestRepository,
                returnBusinessRepository, orderGateway, returnStatusHistoryRepository);
    }

    private ReturnRequest requestWithItems(int... quantities) {
        ReturnRequest request = new ReturnRequest();
        request.setOrderId(100L);
        request.setUserId(1L);
        java.util.ArrayList<ReturnItem> items = new java.util.ArrayList<>();
        for (int quantity : quantities) {
            ReturnItem item = new ReturnItem();
            item.setItemId(25);
            item.setQuantity(quantity);
            item.setReason(ReturnReason.ITEM_DEFECTUOSO);
            items.add(item);
        }
        request.setItems(items);
        return request;
    }

    private void mockValidOrder() {
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(
                new OrderInfo(100L, 1L, LocalDateTime.now().minusDays(2), List.of())));
        when(returnBusinessRepository.pedidoValido(100L, 1L)).thenReturn(true);
        when(returnBusinessRepository.obtenerProductoDelPedido(100L, 25L))
                .thenAnswer(invocation -> List.of("Teclado mecánico", 2));
        when(returnRequestRepository.sumarCantidadSolicitada(eq(100L), eq(25), anyCollection()))
                .thenReturn(0L);
        when(returnRequestRepository.save(any(ReturnRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void crearSolicitud_validaAsociaArticulosYGuardaComoPendiente() {
        mockValidOrder();
        ReturnRequest request = requestWithItems(1);

        ReturnRequest result = service.crearSolicitud(request);

        assertSame(request, result);
        assertEquals(Status.PENDIENTE, result.getStatus());
        assertEquals("Teclado mecánico", result.getItems().get(0).getItemName());
        assertSame(request, result.getItems().get(0).getReturnRequest());
        verify(returnRequestRepository).save(request);
        verify(returnRequestRepository).sumarCantidadSolicitada(eq(100L), eq(25), anyCollection());
    }

    @Test
    void crearSolicitud_agrupaArticulosRepetidosYAsignaNombreATodos() {
        mockValidOrder();
        ReturnRequest request = requestWithItems(1, 1);

        service.crearSolicitud(request);

        assertEquals(2, request.getItems().size());
        assertTrue(request.getItems().stream().allMatch(i -> "Teclado mecánico".equals(i.getItemName())));
        assertTrue(request.getItems().stream().allMatch(i -> i.getReturnRequest() == request));
        verify(returnRequestRepository, times(1))
                .sumarCantidadSolicitada(eq(100L), eq(25), anyCollection());
    }

    @Test
    void crearSolicitud_rechazaPedidoInexistente() {
        when(orderGateway.findOrder(100L)).thenReturn(Optional.empty());
        assertEquals("El pedido no existe.", assertThrows(IllegalArgumentException.class,
                () -> service.crearSolicitud(requestWithItems(1))).getMessage());
        verifyNoInteractions(returnBusinessRepository);
        verify(returnRequestRepository, never()).save(any());
    }

    @Test
    void crearSolicitud_rechazaPedidoDeOtroUsuario() {
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(
                new OrderInfo(100L, 2L, LocalDateTime.now().minusDays(2), List.of())));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.crearSolicitud(requestWithItems(1))).getMessage().contains("no pertenece"));
        verifyNoInteractions(returnBusinessRepository);
    }

    @Test
    void crearSolicitud_rechazaPedidoSinFechaDeCompra() {
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(new OrderInfo(100L, 1L, null, List.of())));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.crearSolicitud(requestWithItems(1))).getMessage().contains("no tiene fecha"));
        verifyNoInteractions(returnBusinessRepository);
    }

    @Test
    void crearSolicitud_rechazaCompraFueraDePlazo() {
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(
                new OrderInfo(100L, 1L, LocalDateTime.now().minusDays(31), List.of())));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.crearSolicitud(requestWithItems(1))).getMessage().contains("más de 30 días"));
        verifyNoInteractions(returnBusinessRepository);
    }

    @Test
    void crearSolicitud_rechazaPedidoInvalidadoPorRepositorioDeNegocio() {
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(
                new OrderInfo(100L, 1L, LocalDateTime.now().minusDays(2), List.of())));
        when(returnBusinessRepository.pedidoValido(100L, 1L)).thenReturn(false);
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.crearSolicitud(requestWithItems(1))).getMessage().contains("cancelado"));
        verify(returnRequestRepository, never()).save(any());
    }

    @Test
    void crearSolicitud_rechazaListaDeArticulosNulaOVacia() {
        mockValidOrder();
        ReturnRequest nullItems = requestWithItems(1);
        nullItems.setItems(null);
        assertThrows(IllegalArgumentException.class, () -> service.crearSolicitud(nullItems));

        ReturnRequest emptyItems = requestWithItems(1);
        emptyItems.setItems(List.of());
        assertThrows(IllegalArgumentException.class, () -> service.crearSolicitud(emptyItems));
        verify(returnRequestRepository, never()).save(any());
    }

    @Test
    void crearSolicitud_rechazaArticuloYaSolicitadoEnEstadoActivo() {
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(
                new OrderInfo(100L, 1L, LocalDateTime.now().minusDays(2), List.of())));
        when(returnBusinessRepository.pedidoValido(100L, 1L)).thenReturn(true);
        when(returnRequestRepository.sumarCantidadSolicitada(eq(100L), eq(25), anyCollection()))
                .thenReturn(1L);

        assertTrue(assertThrows(DuplicateReturnRequestException.class,
                () -> service.crearSolicitud(requestWithItems(1)))
                .getMessage().contains("Ya existe una solicitud"));

        verify(returnBusinessRepository, never()).obtenerProductoDelPedido(anyLong(), anyLong());
    }

    @Test
    void crearSolicitud_rechazaProductoQueNoPerteneceAlPedido() {
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(
                new OrderInfo(100L, 1L, LocalDateTime.now().minusDays(2), List.of())));
        when(returnBusinessRepository.pedidoValido(100L, 1L)).thenReturn(true);
        when(returnRequestRepository.sumarCantidadSolicitada(eq(100L), eq(25), anyCollection())).thenReturn(0L);
        when(returnBusinessRepository.obtenerProductoDelPedido(100L, 25L)).thenReturn(List.of());
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.crearSolicitud(requestWithItems(1))).getMessage().contains("no pertenece"));
        verify(returnRequestRepository, never()).save(any());
    }

    @Test
    void crearSolicitud_rechazaCantidadMayorAUnidadesDisponibles() {
        mockValidOrder();
        ReturnRequest request = requestWithItems(3);
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.crearSolicitud(request)).getMessage().contains("supera las unidades disponibles"));
        verify(returnRequestRepository, never()).save(any());
    }

    @Test
    void obtenerSolicitudPorId_delegaAlRepositorio() {
        ReturnRequest request = new ReturnRequest();
        when(returnRequestRepository.findById(9L)).thenReturn(Optional.of(request));
        assertSame(request, service.obtenerSolicitudPorId(9L).orElseThrow());
        when(returnRequestRepository.findById(10L)).thenReturn(Optional.empty());
        assertTrue(service.obtenerSolicitudPorId(10L).isEmpty());
    }

    @Test
    void obtenerSolicitudesPorUsuario_mapeaLaPaginaAResumenes() {
        Pageable pageable = PageRequest.of(0, 10);
        ReturnRequest request = new ReturnRequest();
        request.setId(1L);
        request.setOrderId(100L);
        request.setUserId(1L);
        request.setStatus(Status.PENDIENTE);
        request.setCreatedAt(LocalDateTime.now().minusHours(2));
        request.setUpdatedAt(LocalDateTime.now());
        when(returnRequestRepository.findByUserId(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(request), pageable, 1));

        Page<ReturnSummaryDTO> result = service.obtenerSolicitudesPorUsuario(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).id());
        assertEquals(100L, result.getContent().get(0).orderId());
        assertEquals(Status.PENDIENTE, result.getContent().get(0).status());
        assertEquals(request.getCreatedAt(), result.getContent().get(0).createdAt());
        assertEquals(request.getUpdatedAt(), result.getContent().get(0).updatedAt());
    }

    @Test
    void obtenerSolicitudesPorUsuario_rechazaIdsInvalidos() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThrows(IllegalArgumentException.class, () -> service.obtenerSolicitudesPorUsuario(null, pageable));
        assertThrows(IllegalArgumentException.class, () -> service.obtenerSolicitudesPorUsuario(0L, pageable));
        assertThrows(IllegalArgumentException.class, () -> service.obtenerSolicitudesPorUsuario(-1L, pageable));
        verifyNoInteractions(returnRequestRepository);
    }

    @Test
    void obtenerSolicitudesPorUsuario_devuelvePaginaVacia() {
        Pageable pageable = PageRequest.of(0, 10);
        when(returnRequestRepository.findByUserId(101L, pageable)).thenReturn(Page.empty(pageable));
        assertTrue(service.obtenerSolicitudesPorUsuario(101L, pageable).isEmpty());
    }

    @Test
    void registrarRecepcion_cambiaARecibidoCuandoEstaAprobada() {
        ReturnRequest request = new ReturnRequest();
        request.setStatus(Status.APROBADO);
        when(returnRequestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(returnRequestRepository.save(request)).thenReturn(request);
        assertSame(request, service.registrarRecepcion(5L));
        assertEquals(Status.RECIBIDO, request.getStatus());
        verify(returnRequestRepository).save(request);
    }

    @Test
    void registrarRecepcion_fallaSiNoExisteLaSolicitud() {
        when(returnRequestRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.registrarRecepcion(404L));
        verify(returnRequestRepository, never()).save(any());
    }

    @Test
    void registrarRecepcion_fallaSiElEstadoNoEsAprobado() {
        ReturnRequest request = new ReturnRequest();
        request.setStatus(Status.RECHAZADO);
        when(returnRequestRepository.findById(5L)).thenReturn(Optional.of(request));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> service.registrarRecepcion(5L)).getMessage().contains("estado APROBADO"));
        verify(returnRequestRepository, never()).save(any());
    }

    @Test
    void decidirDevolucion_apruebaYGuardaHistorial() {
        ReturnRequest request = new ReturnRequest();
        request.setStatus(Status.PENDIENTE);
        when(returnRequestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
        service.decidirDevolucion(1L, Status.APROBADO, "admin");
        assertEquals(Status.APROBADO, request.getStatus());
        verify(returnRequestRepository).save(request);
        ArgumentCaptor<ReturnStatusHistory> captor = ArgumentCaptor.forClass(ReturnStatusHistory.class);
        verify(returnStatusHistoryRepository).save(captor.capture());
        ReturnStatusHistory history = captor.getValue();
        assertEquals(Status.PENDIENTE, history.getPreviousStatus());
        assertEquals(Status.APROBADO, history.getNewStatus());
        assertEquals("admin", history.getChangedBy());
        assertSame(request, history.getReturnRequest());
        assertNotNull(history.getChangedAt());
        assertNull(history.getId());
    }

    @Test
    void decidirDevolucion_rechazaYGuardaHistorial() {
        ReturnRequest request = new ReturnRequest();
        request.setStatus(Status.PENDIENTE);
        when(returnRequestRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(request));
        service.decidirDevolucion(2L, Status.RECHAZADO, "admin");
        assertEquals(Status.RECHAZADO, request.getStatus());
        ArgumentCaptor<ReturnStatusHistory> captor = ArgumentCaptor.forClass(ReturnStatusHistory.class);
        verify(returnStatusHistoryRepository).save(captor.capture());
        assertEquals(Status.RECHAZADO, captor.getValue().getNewStatus());
    }

    @Test
    void decidirDevolucion_rechazaDecisionNulaOPendiente() {
        assertThrows(IllegalArgumentException.class, () -> service.decidirDevolucion(1L, null, "admin"));
        assertThrows(IllegalArgumentException.class, () -> service.decidirDevolucion(1L, Status.PENDIENTE, "admin"));
        verify(returnRequestRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void decidirDevolucion_fallaSiNoEncuentraLaSolicitud() {
        when(returnRequestRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.decidirDevolucion(404L, Status.APROBADO, "admin"));
        verify(returnRequestRepository, never()).save(any());
        verifyNoInteractions(returnStatusHistoryRepository);
    }

    @Test
    void decidirDevolucion_fallaSiNoEstaPendiente() {
        ReturnRequest request = new ReturnRequest();
        request.setStatus(Status.RECIBIDO);
        when(returnRequestRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(request));
        assertThrows(IllegalStateException.class, () -> service.decidirDevolucion(6L, Status.APROBADO, "admin"));
        verify(returnRequestRepository, never()).save(any());
        verifyNoInteractions(returnStatusHistoryRepository);
    }


    @Test
    void crearSolicitud_conProductosDiferentesAsignaCadaNombreCorrectamente() {
        ReturnRequest request = requestWithItems(1, 1);
        request.getItems().get(1).setItemId(30);
        when(orderGateway.findOrder(100L)).thenReturn(Optional.of(
                new OrderInfo(100L, 1L, LocalDateTime.now().minusDays(2), List.of())));
        when(returnBusinessRepository.pedidoValido(100L, 1L)).thenReturn(true);
        when(returnRequestRepository.sumarCantidadSolicitada(eq(100L), anyInt(), anyCollection())).thenReturn(0L);
        when(returnBusinessRepository.obtenerProductoDelPedido(100L, 25L))
                .thenAnswer(invocation -> List.of("Teclado mecánico", 2));
        when(returnBusinessRepository.obtenerProductoDelPedido(100L, 30L))
                .thenAnswer(invocation -> List.of("Mouse inalámbrico", 3));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.crearSolicitud(request);

        assertEquals("Teclado mecánico", request.getItems().get(0).getItemName());
        assertEquals("Mouse inalámbrico", request.getItems().get(1).getItemName());
    }
}
