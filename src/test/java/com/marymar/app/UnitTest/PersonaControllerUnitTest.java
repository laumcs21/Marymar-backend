package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.Service.PersonaService;
import com.marymar.app.controller.PersonaController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonaControllerUnitTest {

    @Mock
    private PersonaService personaService;

    private PersonaController controller;

    @BeforeEach
    void setUp() {
        controller = new PersonaController(personaService);
    }

    @Test
    void obtenerTodasDeberiaRetornar200() {
        when(personaService.obtenerTodas()).thenReturn(List.of(new PersonaResponseDTO()));

        ResponseEntity<List<PersonaResponseDTO>> resultado = controller.obtenerTodas();

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(1, resultado.getBody().size());
    }

    @Test
    void obtenerPorIdDeberiaRetornar200() {
        PersonaResponseDTO dto = new PersonaResponseDTO();
        dto.setId(1L);
        when(personaService.obtenerPorId(1L)).thenReturn(dto);

        ResponseEntity<PersonaResponseDTO> resultado = controller.obtenerPorId(1L);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(dto, resultado.getBody());
    }

    @Test
    void miPerfilDeberiaUsarPrincipal() {
        Principal principal = () -> "laura@test.com";
        PersonaResponseDTO dto = new PersonaResponseDTO();
        dto.setEmail("laura@test.com");
        when(personaService.obtenerPorEmail("laura@test.com")).thenReturn(dto);

        ResponseEntity<PersonaResponseDTO> resultado = controller.miPerfil(principal);

        assertEquals("laura@test.com", resultado.getBody().getEmail());
    }

    @Test
    void crearDesdeAdminDeberiaDelegarYRetornar200() {
        PersonaCreateDTO request = new PersonaCreateDTO();
        PersonaResponseDTO response = new PersonaResponseDTO();
        when(personaService.crear(request)).thenReturn(response);

        ResponseEntity<PersonaResponseDTO> resultado = controller.crearDesdeAdmin(request);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void actualizarDeberiaDelegarYRetornar200() {
        PersonaCreateDTO request = new PersonaCreateDTO();
        PersonaResponseDTO response = new PersonaResponseDTO();
        when(personaService.actualizar(5L, request)).thenReturn(response);

        ResponseEntity<PersonaResponseDTO> resultado = controller.actualizar(5L, request);

        assertEquals(200, resultado.getStatusCode().value());
        assertSame(response, resultado.getBody());
    }

    @Test
    void actualizarMiPerfilDeberiaBuscarActualYActualizar() {
        Principal principal = () -> "laura@test.com";
        PersonaCreateDTO request = new PersonaCreateDTO();
        PersonaResponseDTO actual = new PersonaResponseDTO();
        actual.setId(7L);
        PersonaResponseDTO actualizado = new PersonaResponseDTO();
        actualizado.setId(7L);
        when(personaService.obtenerPorEmail("laura@test.com")).thenReturn(actual);
        when(personaService.actualizar(7L, request)).thenReturn(actualizado);

        ResponseEntity<PersonaResponseDTO> resultado = controller.actualizarMiPerfil(principal, request);

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals(7L, resultado.getBody().getId());
    }

    @Test
    void eliminarDeberiaRetornar204() {
        ResponseEntity<Void> resultado = controller.eliminar(8L);

        verify(personaService).eliminar(8L);
        assertEquals(204, resultado.getStatusCode().value());
    }

    @Test
    void cambiarEstadoDeberiaRetornar204() {
        ResponseEntity<Void> resultado = controller.cambiarEstado(8L, false);

        verify(personaService).cambiarEstado(8L, false);
        assertEquals(204, resultado.getStatusCode().value());
    }
}
