package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Mapper.PersonaMapper;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonaDAOUnitTest {

    @Mock
    private PersonaRepository repository;
    @Mock
    private PersonaMapper mapper;

    @InjectMocks
    private PersonaDAO personaDAO;

    private Persona persona;
    private PersonaCreateDTO createDTO;
    private PersonaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        persona = new Persona();
        persona.setId(1L);
        persona.setEmail("laura@test.com");
        createDTO = new PersonaCreateDTO();
        responseDTO = new PersonaResponseDTO();
        responseDTO.setId(1L);
    }

    @Test
    void crearDeberiaMapearGuardarYRetornarDto() {
        when(mapper.toEntity(createDTO)).thenReturn(persona);
        when(repository.save(persona)).thenReturn(persona);
        when(mapper.toDTO(persona)).thenReturn(responseDTO);

        PersonaResponseDTO resultado = personaDAO.crear(createDTO);

        assertEquals(1L, resultado.getId());
        verify(repository).save(persona);
    }

    @Test
    void obtenerEntidadPorIdDeberiaRetornarPersona() {
        when(repository.findById(1L)).thenReturn(Optional.of(persona));

        Persona resultado = personaDAO.obtenerEntidadPorId(1L);

        assertSame(persona, resultado);
    }

    @Test
    void obtenerEntidadPorIdDeberiaFallarSiNoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> personaDAO.obtenerEntidadPorId(1L));

        assertEquals("Persona no encontrada con id 1", ex.getMessage());
    }

    @Test
    void obtenerEntidadPorEmailDeberiaRetornarPersona() {
        when(repository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));

        Persona resultado = personaDAO.obtenerEntidadPorEmail("laura@test.com");

        assertSame(persona, resultado);
    }

    @Test
    void obtenerEntidadPorEmailDeberiaFallarSiNoExiste() {
        when(repository.findByEmail("laura@test.com")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> personaDAO.obtenerEntidadPorEmail("laura@test.com"));

        assertEquals("Persona no encontrada con email laura@test.com", ex.getMessage());
    }

    @Test
    void obtenerPorIdYPorEmailDeberianMapearDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(persona));
        when(repository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));
        when(mapper.toDTO(persona)).thenReturn(responseDTO);

        assertSame(responseDTO, personaDAO.obtenerPorId(1L));
        assertSame(responseDTO, personaDAO.obtenerPorEmail("laura@test.com"));
    }

    @Test
    void obtenerTodasDeberiaMapearLista() {
        when(repository.findAll()).thenReturn(List.of(persona));
        when(mapper.toDTO(persona)).thenReturn(responseDTO);

        List<PersonaResponseDTO> resultado = personaDAO.obtenerTodas();

        assertEquals(1, resultado.size());
    }

    @Test
    void actualizarDeberiaModificarYGuardar() {
        when(repository.findById(1L)).thenReturn(Optional.of(persona));
        when(repository.save(persona)).thenReturn(persona);
        when(mapper.toDTO(persona)).thenReturn(responseDTO);

        PersonaResponseDTO resultado = personaDAO.actualizar(1L, createDTO);

        assertSame(responseDTO, resultado);
        verify(mapper).updateFromDTO(persona, createDTO);
        verify(repository).save(persona);
    }

    @Test
    void cambiarEstadoDeberiaGuardarNuevoEstado() {
        when(repository.findById(1L)).thenReturn(Optional.of(persona));

        personaDAO.cambiarEstado(1L, false);

        assertFalse(persona.isActivo());
        verify(repository).save(persona);
    }

    @Test
    void eliminarDeberiaBorrarPersonaExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(persona));

        personaDAO.eliminar(1L);

        verify(repository).delete(persona);
    }

    @Test
    void eliminarDeberiaFallarSiPersonaNoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> personaDAO.eliminar(1L));

        assertEquals("Persona no encontrada con id 1", ex.getMessage());
    }

    @Test
    void metodosDeExistenciaDeberianDelegarAlRepositorio() {
        when(repository.findByEmail("laura@test.com")).thenReturn(Optional.of(persona));
        when(repository.existsByEmailAndIdNot("laura@test.com", 2L)).thenReturn(true);
        when(repository.existsByNumeroIdentificacion("123")).thenReturn(true);
        when(repository.existsByNumeroIdentificacionAndIdNot("123", 2L)).thenReturn(true);

        assertTrue(personaDAO.existeEmail("laura@test.com"));
        assertTrue(personaDAO.existeEmailEnOtroUsuario("laura@test.com", 2L));
        assertTrue(personaDAO.existeNumeroIdentificacion("123"));
        assertTrue(personaDAO.existeNumeroIdentificacionEnOtroUsuario("123", 2L));
    }
}
