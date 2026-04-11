package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.business.DTO.InventarioUpdateDTO;
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

    @Mock private InventarioService inventarioService;
    private InventarioController controller;

    @BeforeEach
    void setUp() { controller = new InventarioController(inventarioService); }

    @Test
    void crearDeberiaRetornar200() {
        InventarioCreateDTO dto = new InventarioCreateDTO(1L, 20);
        InventarioResponseDTO response = new InventarioResponseDTO(1L, 1L, "Harina", "kg", 20, LocalDateTime.now(), LocalDateTime.now());
        when(inventarioService.crear(dto)).thenReturn(response);

        ResponseEntity<InventarioResponseDTO> resultado = controller.crear(dto);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void actualizarDeberiaRetornarInventarioActualizado() {
        InventarioUpdateDTO dto = new InventarioUpdateDTO(30);
        InventarioResponseDTO response = new InventarioResponseDTO(1L, 1L, "Harina", "kg", 30, LocalDateTime.now(), LocalDateTime.now());
        when(inventarioService.actualizar(1L, dto)).thenReturn(response);

        ResponseEntity<InventarioResponseDTO> resultado = controller.actualizar(1L, dto);

        assertEquals(30, resultado.getBody().getStock());
    }

    @Test
    void eliminarDeberiaRetornar204() {
        ResponseEntity<Void> resultado = controller.eliminar(5L);
        assertEquals(204, resultado.getStatusCode().value());
        verify(inventarioService).eliminar(5L);
    }

    @Test
    void obtenerTodosDeberiaRetornarLista() {
        when(inventarioService.obtenerTodos()).thenReturn(List.of(new InventarioResponseDTO()));
        assertEquals(1, controller.obtenerTodos().getBody().size());
    }
}
