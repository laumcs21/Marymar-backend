package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.Service.impl.PersonaServiceImpl;
import com.marymar.app.persistence.DAO.PersonaDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PersonaServiceTest {

    @Mock
    private PersonaDAO personaDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PersonaServiceImpl personaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);}

        @Test
        void deberiaCrearUsuarioCorrectamente() {

            PersonaCreateDTO dto = new PersonaCreateDTO(
                    "123456",
                    "Laura",
                    "laura@test.com",
                    "Admin123!",
                    "3001234567",
                    LocalDate.of(2000,1,1),
                    "CLIENTE",
                    "Calle 123",
                    null
            );

            when(passwordEncoder.encode(any())).thenReturn("HASHED");
            when(personaDAO.crear(any())).thenReturn(new PersonaResponseDTO());

            PersonaResponseDTO response = personaService.crear(dto);

            assertNotNull(response);
            verify(passwordEncoder).encode("Admin123!");
            verify(personaDAO).crear(any());
        }

    @Test
    void noDeberiaCrearSiContrasenaInvalida() {

        PersonaCreateDTO dto = new PersonaCreateDTO();
        dto.setContrasena("123"); // inválida
        dto.setEmail("test@test.com");
        dto.setNombre("Test");
        dto.setNumeroIdentificacion("111");
        dto.setFechaNacimiento(LocalDate.of(2000,1,1));
        dto.setRol("CLIENTE");
        dto.setDireccionEnvio("Calle");

        assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(dto));
    }

    @Test
    void noDeberiaPermitirMeseroMenorEdad() {

        PersonaCreateDTO dto = new PersonaCreateDTO(
                "123",
                "Pedro",
                "pedro@test.com",
                "Admin123!",
                "300",
                LocalDate.now(), // edad 0
                "MESERO",
                null,
                1000000.0
        );

        assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(dto));
    }

    @Test
    void clienteDebeTenerDireccion() {

        PersonaCreateDTO dto = new PersonaCreateDTO(
                "123",
                "Ana",
                "ana@test.com",
                "Admin123!",
                "300",
                LocalDate.of(2000,1,1),
                "CLIENTE",
                null,
                null
        );

        assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(dto));
    }


}

