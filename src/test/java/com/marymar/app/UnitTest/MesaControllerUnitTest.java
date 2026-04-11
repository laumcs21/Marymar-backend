package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.MesaCreateDTO;
import com.marymar.app.business.DTO.MesaResponseDTO;
import com.marymar.app.business.Service.MesaService;
import com.marymar.app.controller.MesaController;
import com.marymar.app.persistence.Entity.EstadoMesa;
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
class MesaControllerUnitTest {

    @Mock private MesaService mesaService;
    private MesaController controller;

    @BeforeEach
    void setUp() { controller = new MesaController(mesaService); }

    private MesaResponseDTO mesaResponse() {
        MesaResponseDTO dto = new MesaResponseDTO();
        dto.setId(1L);
        dto.setNumero(8);
        dto.setCapacidad(4);
        dto.setEstado(EstadoMesa.DISPONIBLE);
        dto.setActiva(true);
        return dto;
    }

    @Test
    void crearDeberiaRetornarMesaCreada() {
        MesaCreateDTO dto = new MesaCreateDTO(8, 4);
        MesaResponseDTO response = mesaResponse();
        when(mesaService.crearMesa(dto)).thenReturn(response);

        ResponseEntity<MesaResponseDTO> resultado = controller.crear(dto);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void listarDeberiaRetornarMesas() {
        when(mesaService.listar()).thenReturn(List.of(mesaResponse()));
        assertEquals(1, controller.listar().getBody().size());
    }

    @Test
    void abrirMesaDeberiaDelegarCorrectamente() {
        MesaResponseDTO response = mesaResponse();
        response.setEstado(EstadoMesa.OCUPADA);
        when(mesaService.abrirMesa(1L, 2L)).thenReturn(response);

        ResponseEntity<MesaResponseDTO> resultado = controller.abrirMesa(1L, 2L);

        assertEquals(EstadoMesa.OCUPADA, resultado.getBody().getEstado());
    }

    @Test
    void eliminarDeberiaRetornar204() {
        ResponseEntity<Void> resultado = controller.eliminar(1L);
        assertEquals(204, resultado.getStatusCode().value());
        verify(mesaService).eliminarMesa(1L);
    }
}
