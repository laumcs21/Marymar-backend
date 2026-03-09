package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Mapper.PersonaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PersonaMapperUnitTest {

    private PersonaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PersonaMapper();
    }

    @Test
    void toEntityDeberiaRetornarNullSiDtoEsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntityDeberiaMapearCampos() {
        PersonaCreateDTO dto = new PersonaCreateDTO(
                "123",
                "Laura",
                "laura@test.com",
                "Admin123!",
                "300",
                LocalDate.of(2000, 1, 1),
                "CLIENTE",
                "Calle 123",
                null
        );

        Persona persona = mapper.toEntity(dto);

        assertEquals("123", persona.getNumeroIdentificacion());
        assertEquals("Laura", persona.getNombre());
        assertEquals(Rol.CLIENTE, persona.getRol());
        assertTrue(persona.isActivo());
    }

    @Test
    void toDTODeberiaRetornarNullSiEntidadEsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTODeberiaMapearCamposYAuditoria() {
        Persona persona = Persona.builder()
                .numeroIdentificacion("123")
                .nombre("Laura")
                .email("laura@test.com")
                .contrasena("hash")
                .telefono("300")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .rol(Rol.ADMINISTRADOR)
                .direccionEnvio("Calle 123")
                .salario(2500000.0)
                .activo(true)
                .build();
        persona.setId(1L);
        persona.setCreatedAt(LocalDateTime.now().minusDays(1));
        persona.setUpdatedAt(LocalDateTime.now());

        PersonaResponseDTO dto = mapper.toDTO(persona);

        assertEquals(1L, dto.getId());
        assertEquals("Laura", dto.getNombre());
        assertEquals(Rol.ADMINISTRADOR, dto.getRol());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
    }

    @Test
    void updateFromDTODeberiaActualizarSoloCamposInformados() {
        Persona persona = Persona.builder()
                .numeroIdentificacion("123")
                .nombre("Laura")
                .email("laura@test.com")
                .contrasena("hash")
                .telefono("300")
                .fechaNacimiento(LocalDate.of(2000, 1, 1))
                .rol(Rol.CLIENTE)
                .direccionEnvio("Vieja")
                .activo(true)
                .build();

        PersonaCreateDTO dto = new PersonaCreateDTO();
        dto.setNombre("Laura Updated");
        dto.setEmail("nuevo@test.com");
        dto.setContrasena("nuevoHash");
        dto.setTelefono("301");
        dto.setDireccionEnvio("Nueva");
        dto.setSalario(2000000.0);
        dto.setRol("MESERO");

        mapper.updateFromDTO(persona, dto);

        assertEquals("Laura Updated", persona.getNombre());
        assertEquals("nuevo@test.com", persona.getEmail());
        assertEquals("nuevoHash", persona.getContrasena());
        assertEquals("301", persona.getTelefono());
        assertEquals("Nueva", persona.getDireccionEnvio());
        assertEquals(Double.valueOf(2000000.0), persona.getSalario());
        assertEquals(Rol.MESERO, persona.getRol());
    }
}
