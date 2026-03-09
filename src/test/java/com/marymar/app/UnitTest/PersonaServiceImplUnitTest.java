package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.Service.impl.PersonaServiceImpl;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Mapper.PersonaMapper;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonaServiceImplUnitTest {

    @Mock
    private PersonaDAO personaDAO;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private PersonaServiceImpl personaService;

    private PersonaCreateDTO adminDto;

    @BeforeEach
    void setUp() {
        adminDto = new PersonaCreateDTO(
                "123456",
                "Laura",
                "laura@test.com",
                "Admin123!",
                "3001234567",
                LocalDate.of(2000,1,1),
                "ADMINISTRADOR",
                "Calle 123",
                null
        );
    }

    @Test
    void deberiaCrearUsuarioCorrectamente() {
        PersonaResponseDTO esperado = new PersonaResponseDTO();
        esperado.setNombre("Laura");
        when(passwordEncoder.encode("Admin123!")).thenReturn("HASHED");
        when(personaDAO.crear(any(PersonaCreateDTO.class))).thenReturn(esperado);

        PersonaResponseDTO response = personaService.crear(adminDto);

        assertNotNull(response);
        verify(passwordEncoder).encode("Admin123!");
        verify(personaDAO).crear(any(PersonaCreateDTO.class));
    }

    @Test
    void noDeberiaCrearSiIdentificacionEsObligatoria() {
        adminDto.setNumeroIdentificacion(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("La identificación es obligatoria", ex.getMessage());
    }

    @Test
    void noDeberiaCrearSiNombreEsObligatorio() {
        adminDto.setNombre(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("El nombre es obligatorio", ex.getMessage());
    }

    @Test
    void noDeberiaCrearSiIdentificacionYaExiste() {
        when(personaDAO.existeNumeroIdentificacion("123456")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("La identificación ya está registrada", ex.getMessage());
    }

    @Test
    void noDeberiaCrearSiEmailEsInvalido() {
        adminDto.setEmail("correo-invalido");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("El correo electrónico no es válido", ex.getMessage());
    }

    @Test
    void noDeberiaCrearSiEmailYaExiste() {
        when(personaDAO.existeNumeroIdentificacion("123456")).thenReturn(false);
        when(personaDAO.existeEmail("laura@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("El correo ya está registrado", ex.getMessage());
    }

    @Test
    void noDeberiaCrearSiContrasenaEsInvalida() {
        adminDto.setContrasena("123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertTrue(ex.getMessage().contains("La contraseña debe tener mínimo 6 caracteres"));
    }

    @Test
    void noDeberiaCrearSiFechaNacimientoEsObligatoria() {
        adminDto.setFechaNacimiento(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("La fecha de nacimiento es obligatoria", ex.getMessage());
    }

    @Test
    void noDeberiaCrearSiRolEsObligatorio() {
        adminDto.setRol(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("El rol es obligatorio", ex.getMessage());
    }

    @Test
    void noDeberiaCrearSiRolNoEsValido() {
        adminDto.setRol("GERENTE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("Rol no válido", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirCrearClienteDesdeAdmin() {
        adminDto.setRol("CLIENTE");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("Los clientes deben registrarse públicamente.", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirMeseroMenorEdad() {
        adminDto.setRol("MESERO");
        adminDto.setSalario(1500000.0);
        adminDto.setFechaNacimiento(LocalDate.now().minusYears(17));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("Debe ser mayor de edad para este rol", ex.getMessage());
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.actualizar(1L, dto));

        assertEquals("El cliente debe tener dirección de envío", ex.getMessage());
    }

    @Test
    void meseroDebeTenerSalarioValido() {
        adminDto.setRol("MESERO");
        adminDto.setSalario(0.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.crear(adminDto));

        assertEquals("El mesero debe tener salario válido", ex.getMessage());
    }

    @Test
    void actualizarDeberiaEncriptarContrasenaSiLlegaInformada() {
        when(personaDAO.existeNumeroIdentificacionEnOtroUsuario("123456", 5L)).thenReturn(false);
        when(personaDAO.existeEmailEnOtroUsuario("laura@test.com", 5L)).thenReturn(false);
        when(passwordEncoder.encode("Admin123!")).thenReturn("HASHED");
        when(personaDAO.actualizar(eq(5L), any(PersonaCreateDTO.class))).thenReturn(new PersonaResponseDTO());

        personaService.actualizar(5L, adminDto);

        verify(passwordEncoder).encode("Admin123!");
        verify(personaDAO).actualizar(eq(5L), any(PersonaCreateDTO.class));
    }

    @Test
    void actualizarDeberiaFallarSiIdentificacionExisteEnOtroUsuario() {
        when(personaDAO.existeNumeroIdentificacionEnOtroUsuario("123456", 2L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.actualizar(2L, adminDto));

        assertEquals("La identificación ya está registrada", ex.getMessage());
    }

    @Test
    void actualizarDeberiaFallarSiEmailExisteEnOtroUsuario() {
        when(personaDAO.existeNumeroIdentificacionEnOtroUsuario("123456", 2L)).thenReturn(false);
        when(personaDAO.existeEmailEnOtroUsuario("laura@test.com", 2L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.actualizar(2L, adminDto));

        assertEquals("El correo ya está registrado", ex.getMessage());
    }

    @Test
    void actualizarDeberiaFallarSiNuevaContrasenaEsInvalida() {
        adminDto.setContrasena("abc");
        when(personaDAO.existeNumeroIdentificacionEnOtroUsuario("123456", 2L)).thenReturn(false);
        when(personaDAO.existeEmailEnOtroUsuario("laura@test.com", 2L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.actualizar(2L, adminDto));

        assertTrue(ex.getMessage().contains("La contraseña debe tener mínimo 6 caracteres"));
    }

    @Test
    void obtenerOperacionesDeberianDelegarEnDao() {
        PersonaResponseDTO dto = new PersonaResponseDTO();
        when(personaDAO.obtenerPorId(1L)).thenReturn(dto);
        when(personaDAO.obtenerPorEmail("laura@test.com")).thenReturn(dto);
        when(personaDAO.obtenerTodas()).thenReturn(List.of(dto));

        assertSame(dto, personaService.obtenerPorId(1L));
        assertSame(dto, personaService.obtenerPorEmail("laura@test.com"));
        assertEquals(1, personaService.obtenerTodas().size());
    }

    @Test
    void cambiarEstadoYEliminarDeberianDelegarEnDao() {
        personaService.cambiarEstado(8L, false);
        personaService.eliminar(8L);

        verify(personaDAO).cambiarEstado(8L, false);
        verify(personaDAO).eliminar(8L);
    }

    @Test
    void buscarOCrearUsuarioGoogleDeberiaRetornarExistenteActivo() {
        Persona existente = Persona.builder()
                .email("google@test.com")
                .nombre("Google User")
                .contrasena("x")
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();
        when(personaRepository.findByEmail("google@test.com")).thenReturn(Optional.of(existente));

        Persona response = personaService.buscarOCrearUsuarioGoogle("google@test.com", "Google User");

        assertSame(existente, response);
        verify(personaRepository, never()).save(any());
    }

    @Test
    void buscarOCrearUsuarioGoogleDeberiaFallarSiExisteDesactivado() {
        Persona existente = Persona.builder()
                .email("google@test.com")
                .nombre("Google User")
                .contrasena("x")
                .rol(Rol.CLIENTE)
                .activo(false)
                .build();
        when(personaRepository.findByEmail("google@test.com")).thenReturn(Optional.of(existente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> personaService.buscarOCrearUsuarioGoogle("google@test.com", "Google User"));

        assertEquals("Usuario desactivado.", ex.getMessage());
    }

    @Test
    void buscarOCrearUsuarioGoogleDeberiaCrearNuevoSiNoExiste() {
        when(personaRepository.findByEmail("nuevo@test.com")).thenReturn(Optional.empty());
        when(personaRepository.save(any(Persona.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Persona response = personaService.buscarOCrearUsuarioGoogle("nuevo@test.com", "Nuevo Usuario");

        assertEquals("nuevo@test.com", response.getEmail());
        assertEquals("Nuevo Usuario", response.getNombre());
        assertEquals(Rol.CLIENTE, response.getRol());
        assertTrue(response.isActivo());
        assertEquals("GOOGLE_USER", response.getContrasena());
    }
}
