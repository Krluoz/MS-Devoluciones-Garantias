package com.ecommerce.devoluciones.garantias.controller;

import com.ecommerce.devoluciones.garantias.config.UserIdResolver;
import com.ecommerce.devoluciones.garantias.dto.ReturnRequestCreateDto;
import com.ecommerce.devoluciones.garantias.dto.ReturnItemRequestDto;
import com.ecommerce.devoluciones.garantias.model.ReturnItem;
import com.ecommerce.devoluciones.garantias.model.ReturnRequest;
import com.ecommerce.devoluciones.garantias.model.Status;
import com.ecommerce.devoluciones.garantias.service.ReturnRequestService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReturnRequestController.class)
@Import(TestControllerConfig.class)
class ReturnRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReturnRequestService returnRequestService;

    @MockitoBean
    private UserIdResolver userIdResolver;

    private ReturnRequest solicitud;

    @BeforeEach
    void setUp() {
        solicitud = new ReturnRequest();
        solicitud.setId(1L);
        solicitud.setOrderId(10L);
        solicitud.setUserId(101L);
        solicitud.setStatus(Status.PENDIENTE);
        solicitud.setCreatedAt(LocalDateTime.now());
        solicitud.setUpdatedAt(LocalDateTime.now());

        ReturnItem item = new ReturnItem();
        item.setId(1L);
        item.setItemId(5);
        item.setItemName("Producto de prueba");
        item.setQuantity(1);

        // Ajusta este valor si el constructor o setter de tu entidad lo requiere.
        item.setReturnRequest(solicitud);
        solicitud.setItems(List.of(item));
    }

    @Test
    void crearSolicitud_debeRetornar201() throws Exception {
        ReturnItemRequestDto itemDto = new ReturnItemRequestDto(
                5, 1,
                com.ecommerce.devoluciones.garantias.model.ReturnReason.ITEM_DEFECTUOSO
        );

        ReturnRequestCreateDto requestDto = new ReturnRequestCreateDto(
                10L, 101L, List.of(itemDto)
        );

        when(returnRequestService.crearSolicitud(any(ReturnRequest.class)))
                .thenReturn(solicitud);

        mockMvc.perform(post("/api/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(10))
                .andExpect(jsonPath("$.userId").value(101))
                .andExpect(jsonPath("$.status").value("PENDIENTE"))
                .andExpect(jsonPath("$.items[0].itemId").value(5));

        verify(returnRequestService).crearSolicitud(any(ReturnRequest.class));
    }

    @Test
    void obtenerSolicitudPorId_siExiste_debeRetornar200() throws Exception {
        when(returnRequestService.obtenerSolicitudPorId(1L))
                .thenReturn(Optional.of(solicitud));

        mockMvc.perform(get("/api/returns/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(10))
                .andExpect(jsonPath("$.userId").value(101))
                .andExpect(jsonPath("$.items[0].itemId").value(5));
    }

    @Test
    void obtenerSolicitudPorId_siNoExiste_debeRetornar404() throws Exception {
        when(returnRequestService.obtenerSolicitudPorId(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/returns/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("No se encontro la solicitud de devolucion con ID 99."));
    }

    @Test
    void crearSolicitud_conDatosInvalidos_debeRetornar400() throws Exception {
        String jsonInvalido = """
                {
                  "orderId": 0,
                  "userId": 101,
                  "items": []
                }
                """;

        mockMvc.perform(post("/api/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest());

        verify(returnRequestService, never())
                .crearSolicitud(any(ReturnRequest.class));
    }

    @Test
    void obtenerSolicitudPorId_conIdCero_debeRetornar400() throws Exception {
        mockMvc.perform(get("/api/returns/0"))
                .andExpect(status().isBadRequest());

        verify(returnRequestService, never()).obtenerSolicitudPorId(0L);
    }

    @Test
    void obtenerSolicitudPorId_conItemsNulos_debeRetornarListaVacia()
            throws Exception {

        ReturnRequest solicitudSinItems = new ReturnRequest();
        solicitudSinItems.setId(2L);
        solicitudSinItems.setOrderId(20L);
        solicitudSinItems.setUserId(101L);
        solicitudSinItems.setStatus(Status.PENDIENTE);
        solicitudSinItems.setCreatedAt(LocalDateTime.now());
        solicitudSinItems.setUpdatedAt(LocalDateTime.now());
        solicitudSinItems.setItems(null);

        when(returnRequestService.obtenerSolicitudPorId(2L))
                .thenReturn(Optional.of(solicitudSinItems));

        mockMvc.perform(get("/api/returns/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }
}


