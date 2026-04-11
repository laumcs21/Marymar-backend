package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.PagoResponseDTO;
import com.marymar.app.business.Service.PagoService;
import com.marymar.app.controller.PagoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoControllerUnitTest {

    @Mock private PagoService pagoService;
    private PagoController controller;

    @BeforeEach
    void setUp() { controller = new PagoController(pagoService); }

    @Test
    void pagarDeberiaConstruirDtoYDelegar() {
        PagoResponseDTO response = new PagoResponseDTO(1L, "EFECTIVO", new BigDecimal("25000"), LocalDateTime.now(), null);
        MockMultipartFile comprobante = new MockMultipartFile("comprobante", "pago.jpg", "image/jpeg", "img".getBytes());
        when(pagoService.registrarPago(any(), eq(comprobante))).thenReturn(response);

        ResponseEntity<PagoResponseDTO> resultado = controller.pagar(5L, "EFECTIVO", new BigDecimal("25000"), comprobante);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void obtenerPorPedidoDeberiaRetornarPago() {
        PagoResponseDTO response = new PagoResponseDTO(1L, "TARJETA", new BigDecimal("30000"), LocalDateTime.now(), null);
        when(pagoService.obtenerPorPedido(8L)).thenReturn(response);

        ResponseEntity<PagoResponseDTO> resultado = controller.obtenerPorPedido(8L);

        assertEquals("TARJETA", resultado.getBody().getMetodo());
    }
}
