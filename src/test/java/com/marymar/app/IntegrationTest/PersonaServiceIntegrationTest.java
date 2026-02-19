package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.Service.PersonaService;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PersonaServiceIntegrationTest {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private PersonaService personaService;

    @Autowired
    private PersonaRepository personaRepository;

    // =====================================================
    // CREACIÓN EXITOSA
    // =====================================================

    @Test
    void deberiaCrearPersonaYPersistirEnBD() {

        PersonaCreateDTO dto = crearPersonaBase();

        PersonaResponseDTO creada = personaService.crear(dto);

        assertNotNull(creada.getId());
        assertEquals("Laura", creada.getNombre());
        assertEquals("CLIENTE", creada.getRol());
        assertTrue(creada.isActivo());

        // Verificar que realmente quedó en BD
        var entidad = personaRepository.findById(creada.getId()).orElse(null);
        assertNotNull(entidad);
        assertEquals("Laura", entidad.getNombre());
    }

    // =====================================================
    // IDENTIFICACIÓN DUPLICADA
    // =====================================================

    @Test
    void noDeberiaPermitirIdentificacionDuplicada() {

        PersonaCreateDTO dto1 = crearPersonaBase();
        personaService.crear(dto1);

        PersonaCreateDTO dto2 = crearPersonaBase();
        dto2.setEmail("otro@test.com");

        assertThrows(RuntimeException.class, () -> {
            personaService.crear(dto2);
        });
    }

    // =====================================================
    // EMAIL DUPLICADO
    // =====================================================

    @Test
    void noDeberiaPermitirEmailDuplicado() {

        PersonaCreateDTO dto1 = crearPersonaBase();
        personaService.crear(dto1);

        PersonaCreateDTO dto2 = crearPersonaBase();
        dto2.setNumeroIdentificacion("999999");

        assertThrows(RuntimeException.class, () -> {
            personaService.crear(dto2);
        });
    }

    // =====================================================
    // CONTRASEÑA INVÁLIDA
    // =====================================================

    @Test
    void noDeberiaPermitirContrasenaInvalida() {

        PersonaCreateDTO dto = crearPersonaBase();
        dto.setContrasena("123"); // inválida

        assertThrows(RuntimeException.class, () -> {
            personaService.crear(dto);
        });
    }

    // =====================================================
    // CONTRASEÑA DEBE GUARDARSE ENCRIPTADA
    // =====================================================

    @Test
    void laContrasenaDebeGuardarseEncriptada() {

        PersonaCreateDTO dto = crearPersonaBase();

        PersonaResponseDTO creada = personaService.crear(dto);

        var entidad = personaRepository.findById(creada.getId()).orElseThrow();

        assertNotEquals("Abc123$", entidad.getContrasena());
    }

    // =====================================================
    // MÉTODO AUXILIAR
    // =====================================================

    private PersonaCreateDTO crearPersonaBase() {
        PersonaCreateDTO dto = new PersonaCreateDTO();
        dto.setNumeroIdentificacion("123456");
        dto.setNombre("Laura");
        dto.setEmail("laura@test.com");
        dto.setContrasena("Abc123$");
        dto.setTelefono("3000000000");
        dto.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        dto.setRol("CLIENTE");
        dto.setDireccionEnvio("Calle 123");
        return dto;
    }
}
