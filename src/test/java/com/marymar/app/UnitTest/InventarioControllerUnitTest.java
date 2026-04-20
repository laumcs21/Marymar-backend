package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.InventarioService;
import com.marymar.app.controller.InventarioController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioControllerUnitTest {

    @Mock
    private InventarioService inventarioService;

    private InventarioController controller;

    @BeforeEach
    void setUp() {
        controller = new InventarioController(inventarioService);
    }

    @Test
    void crearDeberiaRetornarInventarioCreado() {
        InventarioCreateDTO dto = new InventarioCreateDTO(1L, 20);
        InventarioResponseDTO response = new InventarioResponseDTO(
                1L, 1L, "Harina", "kg", 20, LocalDateTime.now(), LocalDateTime.now()
        );
        when(inventarioService.crear(dto)).thenReturn(response);

        ResponseEntity<InventarioResponseDTO> resultado = controller.crear(dto);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
        verify(inventarioService).crear(dto);
    }

    @Test
    void actualizarDeberiaRetornarInventarioActualizado() {
        InventarioUpdateDTO dto = new InventarioUpdateDTO(30);
        InventarioResponseDTO response = new InventarioResponseDTO(
                1L, 1L, "Harina", "kg", 30, LocalDateTime.now(), LocalDateTime.now()
        );
        when(inventarioService.actualizar(1L, dto)).thenReturn(response);

        ResponseEntity<InventarioResponseDTO> resultado = controller.actualizar(1L, dto);

        assertEquals(200, resultado.getStatusCode().value());
        assertNotNull(resultado.getBody());
        assertEquals(30, resultado.getBody().getStock());
        verify(inventarioService).actualizar(1L, dto);
    }

    @Test
    void obtenerTodosDeberiaRetornarListaDeInventarios() {
        List<InventarioResponseDTO> response = List.of(
                new InventarioResponseDTO(1L, 1L, "Harina", "kg", 20, LocalDateTime.now(), LocalDateTime.now()),
                new InventarioResponseDTO(2L, 2L, "Azucar", "kg", 10, LocalDateTime.now(), LocalDateTime.now())
        );
        when(inventarioService.obtenerTodos()).thenReturn(response);

        ResponseEntity<List<InventarioResponseDTO>> resultado = controller.obtenerTodos();

        assertEquals(200, resultado.getStatusCode().value());
        assertNotNull(resultado.getBody());
        assertEquals(2, resultado.getBody().size());
        verify(inventarioService).obtenerTodos();
    }

    @Test
    void eliminarDeberiaRetornar204() {
        ResponseEntity<Void> resultado = controller.eliminar(5L);

        assertEquals(204, resultado.getStatusCode().value());
        assertNull(resultado.getBody());
        verify(inventarioService).eliminar(5L);
    }

    @Test
    void ingresarStockDeberiaParsearFechaYDelegarAlServicio() {
        String fechaTexto = "2026-04-19T10:15:30";
        LocalDateTime fecha = LocalDateTime.parse(fechaTexto);

        ResponseEntity<Void> resultado = controller.ingresarStock(4L, 12, fechaTexto);

        assertEquals(200, resultado.getStatusCode().value());
        verify(inventarioService).ingresarStock(4L, 12, fecha);
    }

    @Test
    void surtirCocinaDeberiaDelegarAlServicioYRetornar200() {
        ResponseEntity<Void> resultado = controller.surtirCocina(4L, 8);

        assertEquals(200, resultado.getStatusCode().value());
        verify(inventarioService).surtirCocina(4L, 8);
    }

    @Test
    void vistaBodegueroDeberiaRetornarResumenDeInventario() {
        List<InventarioBodegueroDTO> response = List.of(
                new InventarioBodegueroDTO(1L, 10L, "Harina", 30, 12, 18)
        );
        when(inventarioService.obtenerVistaBodeguero()).thenReturn(response);

        ResponseEntity<List<InventarioBodegueroDTO>> resultado = controller.vistaBodeguero();

        assertEquals(200, resultado.getStatusCode().value());
        assertNotNull(resultado.getBody());
        assertEquals(1, resultado.getBody().size());
        assertEquals("Harina", resultado.getBody().get(0).getInsumoNombre());
        verify(inventarioService).obtenerVistaBodeguero();
    }

    @Test
    void obtenerLotesDeberiaRetornarLotesDelInsumo() {
        List<LoteDTO> response = List.of(
                new LoteDTO(1L, 10, 4, "COCINA", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5)),
                new LoteDTO(2L, 20, 20, "BODEGA", LocalDateTime.now(), LocalDateTime.now().plusDays(20))
        );
        when(inventarioService.obtenerLotes(7L)).thenReturn(response);

        ResponseEntity<List<LoteDTO>> resultado = controller.obtenerLotes(7L);

        assertEquals(200, resultado.getStatusCode().value());
        assertNotNull(resultado.getBody());
        assertEquals(2, resultado.getBody().size());
        assertEquals("COCINA", resultado.getBody().get(0).getUbicacion());
        verify(inventarioService).obtenerLotes(7L);
    }
}
