package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.PedidoService;
import com.marymar.app.controller.PedidoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoControllerUnitTest {

    @Mock private PedidoService pedidoService;
    private PedidoController controller;

    @BeforeEach
    void setUp() { controller = new PedidoController(pedidoService); }

    private PedidoResponseDTO pedidoResponse() {
        return new PedidoResponseDTO(
                1L,
                LocalDateTime.now(),
                "CREADO",
                "MESA",
                null,
                2L,
                "Mesero",
                3L,
                8,
                new BigDecimal("25000"),
                List.of(new DetallePedidoResponseDTO(10L, "Mojarra", 1, new BigDecimal("25000"), new BigDecimal("25000"), 5L)),
                null
        );
    }

    @Test
    void crearPedidoDeberiaRetornar200() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 1);
        PedidoResponseDTO response = pedidoResponse();
        when(pedidoService.crearPedido(dto)).thenReturn(response);

        ResponseEntity<PedidoResponseDTO> resultado = controller.crearPedido(dto);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void agregarProductoDeberiaDelegar() {
        PedidoResponseDTO response = pedidoResponse();
        when(pedidoService.agregarProducto(1L, 5L, 2)).thenReturn(response);

        ResponseEntity<PedidoResponseDTO> resultado = controller.agregarProducto(1L, 5L, 2);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void generarFacturaDeberiaRetornarPdfInline() throws Exception {
        PedidoResponseDTO pedido = pedidoResponse();
        when(pedidoService.obtenerPorId(1L)).thenReturn(pedido);

        ResponseEntity<byte[]> resultado = controller.generarFactura(1L);

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, resultado.getHeaders().getContentType());
        assertTrue(resultado.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("factura.pdf"));
        assertNotNull(resultado.getBody());
        assertTrue(resultado.getBody().length > 0);
    }

    @Test
    void generarComandaDeberiaRetornarPdfInline() throws Exception {
        PedidoResponseDTO pedido = pedidoResponse();
        when(pedidoService.obtenerPorId(1L)).thenReturn(pedido);

        ResponseEntity<byte[]> resultado = controller.generarComanda(1L);

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, resultado.getHeaders().getContentType());
        assertTrue(resultado.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("comanda.pdf"));
    }
}
