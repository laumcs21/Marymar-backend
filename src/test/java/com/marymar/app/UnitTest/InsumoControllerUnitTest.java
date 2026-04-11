package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.InsumoCreateDTO;
import com.marymar.app.business.DTO.InsumoResponseDTO;
import com.marymar.app.business.Service.InsumoService;
import com.marymar.app.controller.InsumoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsumoControllerUnitTest {

    @Mock private InsumoService insumoService;
    private InsumoController controller;

    @BeforeEach
    void setUp() {
        controller = new InsumoController(insumoService);
    }

    @Test
    void crearDeberiaRetornar200ConEntidadCreada() {
        InsumoCreateDTO dto = new InsumoCreateDTO("Harina", "kg");
        InsumoResponseDTO response = new InsumoResponseDTO(1L, "Harina", "kg");
        when(insumoService.crear(dto)).thenReturn(response);

        ResponseEntity<InsumoResponseDTO> resultado = controller.crear(dto);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void obtenerTodosDeberiaRetornarLista() {
        when(insumoService.obtenerTodos()).thenReturn(List.of(new InsumoResponseDTO(1L, "Harina", "kg")));

        ResponseEntity<List<InsumoResponseDTO>> resultado = controller.obtenerTodos();

        assertEquals(1, resultado.getBody().size());
    }

    @Test
    void eliminarDeberiaRetornar204() {
        ResponseEntity<Void> resultado = controller.eliminar(5L);
        assertEquals(204, resultado.getStatusCode().value());
        verify(insumoService).eliminar(5L);
    }
}
