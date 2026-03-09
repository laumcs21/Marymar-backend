package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.business.Service.ProductoService;
import com.marymar.app.controller.ProductoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoControllerUnitTest {

    @Mock
    private ProductoService productoService;

    private ProductoController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductoController(productoService);
    }

    @Test
    void crearDeberiaDelegarYRetornarProducto() {
        ProductoCreateDTO request = new ProductoCreateDTO("Mojarra", new BigDecimal("35000"), 1L, "Mojarra frita");
        ProductoResponseDTO response = new ProductoResponseDTO(1L, "Mojarra", new BigDecimal("35000"),
                "Mojarra frita", 1L, "Pescados", true);
        when(productoService.crear(request, null)).thenReturn(response);

        ProductoResponseDTO resultado = controller.crear(request, null);

        assertSame(response, resultado);
    }

    @Test
    void obtenerTodosDeberiaRetornar200() {
        when(productoService.obtenerTodos()).thenReturn(List.of(new ProductoResponseDTO()));

        ResponseEntity<List<ProductoResponseDTO>> resultado = controller.obtenerTodos();

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(1, resultado.getBody().size());
    }

    @Test
    void obtenerPorIdDeberiaRetornar200() {
        ProductoResponseDTO response = new ProductoResponseDTO();
        when(productoService.obtenerPorId(1L)).thenReturn(response);

        ResponseEntity<ProductoResponseDTO> resultado = controller.obtenerPorId(1L);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void actualizarDeberiaRetornar200() {
        ProductoCreateDTO request = new ProductoCreateDTO();
        ProductoResponseDTO response = new ProductoResponseDTO();
        when(productoService.actualizar(1L, request, null)).thenReturn(response);

        ResponseEntity<ProductoResponseDTO> resultado = controller.actualizar(1L, request, null);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void desactivarDeberiaRetornar204() {
        ResponseEntity<Void> resultado = controller.desactivar(1L);

        verify(productoService).desactivar(1L);
        assertEquals(204, resultado.getStatusCode().value());
    }

    @Test
    void obtenerPorCategoriaDeberiaDelegar() {
        when(productoService.obtenerPorCategoria(1L)).thenReturn(List.of(new ProductoResponseDTO()));

        List<ProductoResponseDTO> resultado = controller.obtenerPorCategoria(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void eliminarDefinitivoDeberiaDelegar() {
        controller.eliminarDefinitivo(1L);

        verify(productoService).eliminarDefinitivo(1L);
    }
}
