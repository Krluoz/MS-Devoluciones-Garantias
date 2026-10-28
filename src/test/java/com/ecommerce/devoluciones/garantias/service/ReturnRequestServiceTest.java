
package com.ecommerce.devoluciones.garantias.service;

import com.ecommerce.devoluciones.garantias.dto.ReturnSummaryDTO;
import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.model.Status;
import com.ecommerce.devoluciones.garantias.repository.ReturnBusinessRepository;
import com.ecommerce.devoluciones.garantias.repository.ReturnRequestRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ecommerce.devoluciones.garantias.repository.ReturnStatusHistoryRepository;
import com.ecommerce.devoluciones.garantias.model.ReturnStatusHistory;
import org.mockito.ArgumentCaptor;
import java.util.NoSuchElementException;

class ReturnRequestServiceTest {

    private ReturnRequestRepository returnRequestRepository;
    private ReturnRequestService returnRequestService;
    private ReturnStatusHistoryRepository returnStatusHistoryRepository;

    @BeforeEach
    void setUp() {

        returnRequestRepository =
                mock(ReturnRequestRepository.class);

        ReturnBusinessRepository returnBusinessRepository =
                mock(ReturnBusinessRepository.class);

        returnStatusHistoryRepository =
                mock(ReturnStatusHistoryRepository.class);

        returnRequestService = new ReturnRequestService(
                returnRequestRepository,
                returnBusinessRepository,
                returnStatusHistoryRepository
        );
    }

    @Test
    void debeConsultarDevolucionesDelUsuario101() {

        // Arrange: preparar los datos de prueba
        Long userId = 101L;
        Pageable pageable = PageRequest.of(0, 10);

        ReturnRequest solicitud = new ReturnRequest();
        solicitud.setId(1L);
        solicitud.setOrderId(1001L);
        solicitud.setUserId(101L);
        solicitud.setStatus(Status.PENDIENTE);

        Page<ReturnRequest> pagina =
                new PageImpl<>(List.of(solicitud), pageable, 1);

        when(returnRequestRepository.findByUserId(userId, pageable))
                .thenReturn(pagina);

        // Act: ejecutar el método que queremos probar
        Page<ReturnSummaryDTO> resultado =
                returnRequestService.obtenerSolicitudesPorUsuario(
                        userId, pageable);

        // Assert: verificar los resultados
        assertEquals(1, resultado.getTotalElements());
        assertEquals(1L, resultado.getContent().get(0).id());
        assertEquals(1001L, resultado.getContent().get(0).orderId());
        assertEquals(Status.PENDIENTE,
                resultado.getContent().get(0).status());

        verify(returnRequestRepository, times(1))
                .findByUserId(userId, pageable);
    }

    @Test
    void debeDevolverPaginaVaciaCuandoNoHayDevoluciones() {

        Long userId = 102L;
        Pageable pageable = PageRequest.of(0, 10);

        when(returnRequestRepository.findByUserId(userId, pageable))
                .thenReturn(Page.empty(pageable));

        Page<ReturnSummaryDTO> resultado =
                returnRequestService.obtenerSolicitudesPorUsuario(
                        userId, pageable);

        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.getTotalElements());

        verify(returnRequestRepository, times(1))
                .findByUserId(userId, pageable);
    }

    @Test
    void debeRechazarUserIdNulo() {

        Pageable pageable = PageRequest.of(0, 10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> returnRequestService.obtenerSolicitudesPorUsuario(
                        null, pageable)
        );

        assertEquals(
                "El identificador del usuario no es válido.",
                exception.getMessage()
        );

        verifyNoInteractions(returnRequestRepository);
    }

    @Test
    void debeRechazarUserIdNegativo() {

        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(
                IllegalArgumentException.class,
                () -> returnRequestService.obtenerSolicitudesPorUsuario(
                        -1L, pageable)
        );

        verifyNoInteractions(returnRequestRepository);
    }
    @Test
    void debeRegistrarRecepcionCuandoSolicitudEstaAprobada() {
        ReturnRequest solicitud = new ReturnRequest();
        solicitud.setId(1L);
        solicitud.setStatus(Status.APROBADO);

        when(returnRequestRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(i -> i.getArgument(0));

        ReturnRequest resultado = returnRequestService.registrarRecepcion(1L);

        assertEquals(Status.RECIBIDO, resultado.getStatus());
        verify(returnRequestRepository, times(1)).save(solicitud);
    }

    @Test
    void debeLanzarExcepcionCuandoSolicitudEstaRechazada() {
        ReturnRequest solicitud = new ReturnRequest();
        solicitud.setId(3L);
        solicitud.setStatus(Status.RECHAZADO);

        when(returnRequestRepository.findById(3L)).thenReturn(Optional.of(solicitud));

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> returnRequestService.registrarRecepcion(3L)
        );

        assertTrue(excepcion.getMessage().contains("La devolución debe estar en estado APROBADO"));
        verify(returnRequestRepository, never()).save(any());
    }

    @Test
    void debeLanzarExcepcionCuandoSolicitudNoExiste() {
        when(returnRequestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> returnRequestService.registrarRecepcion(999L)
        );

        verify(returnRequestRepository, never()).save(any());
    }


    @Test
    void decidirDevolucion_cuandoEstaPendiente_debeAprobarYGuardarHistorial() {

        // 1. Preparar una devolución pendiente
        Long id = 1L;

        ReturnRequest devolucion = new ReturnRequest();
        devolucion.setStatus(Status.PENDIENTE);

        // 2. Simular la búsqueda en el repositorio
        when(returnRequestRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(devolucion));

        // 3. Ejecutar el método que queremos probar
        returnRequestService.decidirDevolucion(
                id,
                Status.APROBADO,
                "admin"
        );

        // 4. Comprobar que el estado cambió
        assertEquals(Status.APROBADO, devolucion.getStatus());

        // 5. Verificar que se guardó la devolución
        verify(returnRequestRepository).save(devolucion);

        // 6. Verificar el historial
        ArgumentCaptor<ReturnStatusHistory> captor =
                ArgumentCaptor.forClass(ReturnStatusHistory.class);

        verify(returnStatusHistoryRepository).save(captor.capture());

        ReturnStatusHistory historial = captor.getValue();

        assertEquals(Status.PENDIENTE, historial.getPreviousStatus());
        assertEquals(Status.APROBADO, historial.getNewStatus());
        assertEquals("admin", historial.getChangedBy());
        assertSame(devolucion, historial.getReturnRequest());
    }


    @Test
    void decidirDevolucion_cuandoEstaPendiente_debeRechazarYGuardarHistorial() {

        // 1. Preparar una devolución pendiente
        Long id = 2L;

        ReturnRequest devolucion = new ReturnRequest();
        devolucion.setStatus(Status.PENDIENTE);

        // 2. Simular la búsqueda de la devolución
        when(returnRequestRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(devolucion));

        // 3. Ejecutar la decisión de rechazo
        returnRequestService.decidirDevolucion(
                id,
                Status.RECHAZADO,
                "admin"
        );

        // 4. Verificar el cambio de estado
        assertEquals(Status.RECHAZADO, devolucion.getStatus());

        // 5. Verificar que se guardó la devolución
        verify(returnRequestRepository).save(devolucion);

        // 6. Capturar y verificar el historial
        ArgumentCaptor<ReturnStatusHistory> captor =
                ArgumentCaptor.forClass(ReturnStatusHistory.class);

        verify(returnStatusHistoryRepository).save(captor.capture());

        ReturnStatusHistory historial = captor.getValue();

        assertEquals(Status.PENDIENTE, historial.getPreviousStatus());
        assertEquals(Status.RECHAZADO, historial.getNewStatus());
        assertEquals("admin", historial.getChangedBy());
        assertSame(devolucion, historial.getReturnRequest());
    }


    @Test
    void decidirDevolucion_cuandoNoExiste_debeLanzarExcepcion() {

        // 1. Preparar un ID inexistente
        Long id = 999L;

        // 2. Simular que el repositorio no encuentra la devolución
        when(returnRequestRepository.findByIdForUpdate(id))
                .thenReturn(Optional.empty());

        // 3. Comprobar que el servicio lanza la excepción
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> returnRequestService.decidirDevolucion(
                        id,
                        Status.APROBADO,
                        "admin"
                )
        );

        // 4. Verificar el mensaje
        assertEquals(
                "No se encontró la devolución con ID: 999",
                exception.getMessage()
        );

        // 5. Comprobar que no se guardó ningún cambio
        verify(returnRequestRepository, never()).save(any());
        verify(returnStatusHistoryRepository, never()).save(any());
    }


    @Test
    void decidirDevolucion_cuandoYaEstaAprobada_debeLanzarConflicto() {

        // 1. Preparar una devolución ya aprobada
        Long id = 3L;

        ReturnRequest devolucion = new ReturnRequest();
        devolucion.setStatus(Status.APROBADO);

        // 2. Simular la consulta
        when(returnRequestRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(devolucion));

        // 3. Intentar rechazar la devolución
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> returnRequestService.decidirDevolucion(
                        id,
                        Status.RECHAZADO,
                        "admin"
                )
        );

        // 4. Verificar el mensaje
        assertTrue(exception.getMessage().contains(
                "Solo se pueden decidir devoluciones PENDIENTES"
        ));

        // 5. Verificar que conserva su estado
        assertEquals(Status.APROBADO, devolucion.getStatus());

        // 6. Comprobar que no se guardaron cambios
        verify(returnRequestRepository, never()).save(any());
        verify(returnStatusHistoryRepository, never()).save(any());
    }


    @Test
    void decidirDevolucion_cuandoYaEstaRechazada_debeLanzarConflicto() {

        // 1. Preparar una devolución rechazada
        Long id = 4L;

        ReturnRequest devolucion = new ReturnRequest();
        devolucion.setStatus(Status.RECHAZADO);

        // 2. Simular la búsqueda
        when(returnRequestRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(devolucion));

        // 3. Intentar aprobar una devolución rechazada
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> returnRequestService.decidirDevolucion(
                        id,
                        Status.APROBADO,
                        "admin"
                )
        );

        // 4. Comprobar el mensaje de error
        assertTrue(exception.getMessage().contains(
                "Solo se pueden decidir devoluciones PENDIENTES"
        ));

        // 5. Verificar que conserva su estado
        assertEquals(Status.RECHAZADO, devolucion.getStatus());

        // 6. Verificar que no se guardaron cambios
        verify(returnRequestRepository, never()).save(any());
        verify(returnStatusHistoryRepository, never()).save(any());
    }


    @Test
    void decidirDevolucion_cuandoDecisionEsInvalida_debeLanzarExcepcion() {

        // 1. Preparar una decisión no permitida
        Long id = 5L;

        // 2. Intentar enviar RECIBIDO como decisión
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> returnRequestService.decidirDevolucion(
                        id,
                        Status.RECIBIDO,
                        "admin"
                )
        );

        // 3. Verificar el mensaje
        assertEquals(
                "La decisión debe ser APROBADO o RECHAZADO",
                exception.getMessage()
        );

        // 4. No debe buscar ni modificar devoluciones
        verify(returnRequestRepository, never()).findById(any());
        verify(returnRequestRepository, never()).save(any());
        verify(returnStatusHistoryRepository, never()).save(any());
    }


}
