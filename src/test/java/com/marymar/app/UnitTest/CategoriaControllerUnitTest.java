package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.business.Service.CategoriaService;
import com.marymar.app.controller.CategoriaController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerUnitTest {

    @Mock
    private CategoriaService categoriaService;

    private CategoriaController controller;

    @BeforeEach
    void setUp() {
        controller = new CategoriaController(categoriaService);
    }

    @Test
    void obtenerTodasDeberiaRetornar200() {
        when(categoriaService.obtenerTodas()).thenReturn(List.of(new CategoriaResponseDTO(1L, "Mariscos", 0L)));

        ResponseEntity<List<CategoriaResponseDTO>> resultado = controller.obtenerTodas();

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(1, resultado.getBody().size());
    }

    @Test
    void crearDeberiaRetornar200() {
        CategoriaCreateDTO request = new CategoriaCreateDTO("Mariscos");
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "Mariscos", 0L);
        when(categoriaService.crear(request)).thenReturn(response);

        ResponseEntity<CategoriaResponseDTO> resultado = controller.crear(request);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void actualizarDeberiaRetornar200() {
        CategoriaCreateDTO request = new CategoriaCreateDTO("Actualizada");
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "Actualizada", 0L);
        when(categoriaService.actualizar(1L, request)).thenReturn(response);

        ResponseEntity<CategoriaResponseDTO> resultado = controller.actualizar(1L, request);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void eliminarDeberiaRetornar204() {
        ResponseEntity<Void> resultado = controller.eliminar(1L);

        verify(categoriaService).eliminar(1L);
        assertEquals(204, resultado.getStatusCode().value());
    }
}
