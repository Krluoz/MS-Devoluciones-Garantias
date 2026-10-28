
package com.ecommerce.devoluciones.garantias.service;

import com.ecommerce.devoluciones.garantias.dto.ReturnSummaryDTO;
import com.ecommerce.devoluciones.garantias.gateway.OrderGateway;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReturnRequestServiceTest {

    private ReturnRequestRepository returnRequestRepository;
    private ReturnRequestService returnRequestService;
    private OrderGateway orderGateway;

    @BeforeEach
    void setUp() {

        returnRequestRepository =
                mock(ReturnRequestRepository.class);

        ReturnBusinessRepository returnBusinessRepository =
                mock(ReturnBusinessRepository.class);

        orderGateway = mock(OrderGateway.class);

        returnRequestService = new ReturnRequestService(
                returnRequestRepository,
                returnBusinessRepository,
                orderGateway
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
}
