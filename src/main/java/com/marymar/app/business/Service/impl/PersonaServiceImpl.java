package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.business.Service.PersonaService;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaDAO personaDAO;
    private final PasswordEncoder passwordEncoder;
    private final PersonaRepository personaRepository;

    public PersonaServiceImpl(PersonaDAO personaDAO,
                              PasswordEncoder passwordEncoder, PersonaRepository personaRepository) {
        this.personaDAO = personaDAO;
        this.passwordEncoder = passwordEncoder;
        this.personaRepository = personaRepository;
    }

    // =========================
    // Crear
    // =========================
    @Override
    public PersonaResponseDTO crear(PersonaCreateDTO dto) {

        validarPersona(dto, true);

        dto.setContrasena(
                passwordEncoder.encode(dto.getContrasena())
        );

        return personaDAO.crear(dto);
    }

    // =========================
    // Obtener
    // =========================
    @Override
    public PersonaResponseDTO obtenerPorId(Long id) {
        return personaDAO.obtenerPorId(id);
    }

    @Override
    public PersonaResponseDTO obtenerPorEmail(String email) {
        return personaDAO.obtenerPorEmail(email);
    }

    @Override
    public List<PersonaResponseDTO> obtenerTodas() {
        return personaDAO.obtenerTodas();
    }

    // =========================
    // Actualizar
    // =========================
    @Override
    public PersonaResponseDTO actualizar(Long id, PersonaCreateDTO dto) {

        validarPersona(dto, false);

        if (dto.getContrasena() != null) {
            dto.setContrasena(
                    passwordEncoder.encode(dto.getContrasena())
            );
        }

        return personaDAO.actualizar(id, dto);
    }

    // =========================
    // Desactivar
    // =========================
    @Override
    public void desactivar(Long id) {
        personaDAO.desactivar(id);
    }

    // =========================
    // Eliminar
    // =========================
    @Override
    public void eliminar(Long id) {
        personaDAO.eliminar(id);
    }

    // =========================
    // Login google
    // =========================
    @Override
    public Persona buscarOCrearUsuarioGoogle(String email, String nombre) {

        Optional<Persona> personaOpt = personaRepository.findByEmail(email);

        if (personaOpt.isPresent()) {

            Persona existente = personaOpt.get();

            if (!existente.isActivo()) {
                throw new RuntimeException("Usuario desactivado.");
            }

            return existente;
        }

        Persona nuevaPersona = Persona.builder()
                .numeroIdentificacion("GOOGLE-" + System.currentTimeMillis())
                .nombre(nombre)
                .email(email)
                .contrasena("GOOGLE_USER")
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();

        return personaRepository.save(nuevaPersona);
    }

    // =========================
    // VALIDACIONES
    // =========================
    private void validarPersona(PersonaCreateDTO dto, boolean esNuevo) {

        if (dto.getNumeroIdentificacion() == null ||
                dto.getNumeroIdentificacion().isBlank()) {
            throw new IllegalArgumentException("La identificación es obligatoria");
        }

        if (dto.getNombre() == null || dto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (dto.getEmail() == null || !esEmailValido(dto.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico no es válido");
        }

        if (esNuevo) {
            if (personaDAO.existeEmail(dto.getEmail())) {
                throw new IllegalArgumentException("El correo ya está registrado");
            }
        } else {
            if (dto.getEmail() != null &&
                    personaDAO.existeEmail(dto.getEmail())) {
                throw new IllegalArgumentException("El correo ya está registrado");
            }
        }

        if (esNuevo) {
            if (!esContrasenaValida(dto.getContrasena())) {
                throw new IllegalArgumentException(
                        "La contraseña debe tener mínimo 6 caracteres, " +
                                "una mayúscula, una minúscula, un número y un símbolo"
                );
            }
        } else {
            if (dto.getContrasena() != null &&
                    !esContrasenaValida(dto.getContrasena())) {

                throw new IllegalArgumentException(
                        "La contraseña debe tener mínimo 6 caracteres, " +
                                "una mayúscula, una minúscula, un número y un símbolo"
                );
            }
        }

        if (dto.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }

        if (dto.getRol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }

        Rol rol;
        try {
            rol = Rol.valueOf(dto.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Rol inválido");
        }

        int edad = calcularEdad(dto.getFechaNacimiento());

        if ((rol == Rol.MESERO || rol == Rol.ADMINISTRADOR) && edad < 18) {
            throw new IllegalArgumentException("Debe ser mayor de edad para este rol");
        }

        if (rol == Rol.CLIENTE &&
                (dto.getDireccionEnvio() == null ||
                        dto.getDireccionEnvio().isBlank())) {
            throw new IllegalArgumentException("El cliente debe tener dirección de envío");
        }

        if (rol == Rol.MESERO &&
                (dto.getSalario() == null || dto.getSalario() <= 0)) {
            throw new IllegalArgumentException("El mesero debe tener salario válido");
        }
    }

    private boolean esContrasenaValida(String contrasena) {

        if (contrasena == null) return false;

        String regex =
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{6,}$";

        return Pattern.compile(regex).matcher(contrasena).matches();
    }

    private boolean esEmailValido(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    private int calcularEdad(LocalDate fechaNacimiento) {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}
