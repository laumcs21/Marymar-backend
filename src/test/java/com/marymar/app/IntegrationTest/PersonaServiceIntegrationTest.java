package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.Service.GoogleIdTokenService;
import com.marymar.app.business.Service.PersonaService;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class PersonaServiceIntegrationTest {

    @Autowired
    private PersonaService personaService;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    @Test
    void deberiaCrearPersonaYPersistirEnBD() {
        PersonaCreateDTO dto = crearPersonaBase();

        PersonaResponseDTO creada = personaService.crear(dto);

        assertNotNull(creada.getId());
        assertEquals("Laura", creada.getNombre());
        assertEquals(Rol.ADMINISTRADOR, creada.getRol());
        assertTrue(creada.isActivo());

        Persona entidad = personaRepository.findById(creada.getId()).orElse(null);
        assertNotNull(entidad);
        assertEquals("Laura", entidad.getNombre());
        assertNotEquals("Abc123$", entidad.getContrasena());
    }

    @Test
    void deberiaActualizarSoloCamposPermitidos() {
        PersonaCreateDTO base = crearPersonaBase();
        PersonaResponseDTO creada = personaService.crear(base);

        PersonaCreateDTO update = crearPersonaBase();
        update.setNombre("Laura Editada");
        update.setEmail("laura.editada@test.com");
        update.setNumeroIdentificacion("999999");
        update.setFechaNacimiento(LocalDate.of(1995, 5, 5));
        update.setContrasena("Nueva123$");

        personaService.actualizar(creada.getId(), update);
        entityManager.flush();
        entityManager.clear();

        Persona persona = personaRepository.findById(creada.getId()).orElseThrow();

        assertEquals("Laura Editada", persona.getNombre());
        assertEquals("laura.editada@test.com", persona.getEmail());

        // no deben cambiar
        assertEquals("123456", persona.getNumeroIdentificacion());
        assertEquals(LocalDate.of(2000, 1, 1), persona.getFechaNacimiento());

        assertNotEquals("Nueva123$", persona.getContrasena());
        assertTrue(passwordEncoder.matches("Nueva123$", persona.getContrasena()));
    }

    @Test
    void cambiarEstadoDeberiaPersistirse() {
        PersonaResponseDTO creada = personaService.crear(crearPersonaBase());

        personaService.cambiarEstado(creada.getId(), false);
        entityManager.flush();
        entityManager.clear();

        Persona entidad = personaRepository.findById(creada.getId()).orElseThrow();
        assertFalse(entidad.isActivo());
    }

    @Test
    void eliminarDeberiaRemoverPersona() {
        PersonaResponseDTO creada = personaService.crear(crearPersonaBase());

        personaService.eliminar(creada.getId());
        entityManager.flush();
        entityManager.clear();

        assertTrue(personaRepository.findById(creada.getId()).isEmpty());
    }

    @Test
    void buscarOCrearUsuarioGoogleDeberiaCrearNuevo() {
        Persona persona = personaService.buscarOCrearUsuarioGoogle("google@test.com", "Google User");

        assertNotNull(persona.getNumeroIdentificacion());
        assertTrue(persona.getNumeroIdentificacion().startsWith("GOOGLE-"));
        assertEquals("google@test.com", persona.getEmail());
        assertEquals(Rol.CLIENTE, persona.getRol());
    }

    @Test
    void noDeberiaPermitirEmailDuplicado() {
        personaService.crear(crearPersonaBase());
        PersonaCreateDTO dto2 = crearPersonaBase();
        dto2.setNumeroIdentificacion("999999");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> personaService.crear(dto2));

        assertEquals("El correo ya está registrado", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirContrasenaInvalida() {
        PersonaCreateDTO dto = crearPersonaBase();
        dto.setContrasena("123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> personaService.crear(dto));

        assertTrue(ex.getMessage().contains("La contraseña debe tener mínimo 6 caracteres"));
    }

    private PersonaCreateDTO crearPersonaBase() {
        PersonaCreateDTO dto = new PersonaCreateDTO();
        dto.setNumeroIdentificacion("123456");
        dto.setNombre("Laura");
        dto.setEmail("laura@test.com");
        dto.setContrasena("Abc123$");
        dto.setTelefono("3000000000");
        dto.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        dto.setRol("ADMINISTRADOR");
        dto.setDireccionEnvio("Calle 123");
        return dto;
    }
}
