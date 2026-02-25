package com.marymar.app.persistence.DAO;

import com.marymar.app.business.DTO.PersonaCreateDTO;
import com.marymar.app.business.DTO.PersonaResponseDTO;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Mapper.PersonaMapper;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersonaDAO {

    private final PersonaRepository repository;
    private final PersonaMapper mapper;

    public PersonaDAO(PersonaRepository repository, PersonaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // =========================
    // Crear
    // =========================
    public PersonaResponseDTO crear(PersonaCreateDTO dto) {

        Persona persona = mapper.toEntity(dto);
        Persona guardada = repository.save(persona);

        return mapper.toDTO(guardada);
    }

    // =========================
    // Obtener entidad por ID
    // =========================
    public Persona obtenerEntidadPorId(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id " + id));
    }

    // =========================
    // Obtener entidad por email
    // =========================
    public Persona obtenerEntidadPorEmail(String email) {

        return repository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con email " + email));
    }

    // =========================
    // Obtener por ID (DTO)
    // =========================
    public PersonaResponseDTO obtenerPorId(Long id) {

        Persona persona = obtenerEntidadPorId(id);
        return mapper.toDTO(persona);
    }

    // =========================
    // Obtener por Email (DTO)
    // =========================
    public PersonaResponseDTO obtenerPorEmail(String email) {

        Persona persona = obtenerEntidadPorEmail(email);
        return mapper.toDTO(persona);
    }

    // =========================
    // Obtener todas
    // =========================
    public List<PersonaResponseDTO> obtenerTodas() {

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Actualizar
    // =========================
    public PersonaResponseDTO actualizar(Long id, PersonaCreateDTO dto) {

        Persona existente = obtenerEntidadPorId(id);

        mapper.updateFromDTO(existente, dto);

        Persona actualizada = repository.save(existente);

        return mapper.toDTO(actualizada);
    }

    // =========================
    // Desactivar (borrado lógico)
    // =========================
    public void desactivar(Long id) {

        Persona persona = obtenerEntidadPorId(id);
        persona.setActivo(false);
        repository.save(persona);
    }

    // =========================
    // Eliminar (borrado físico)
    // =========================
    public void eliminar(Long id) {

        Persona persona = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id " + id));

        repository.delete(persona);
    }

    public boolean existeEmail(String email) {
        return repository.findByEmail(email).isPresent();
    }

    public boolean existeEmailEnOtroUsuario(String email, Long idActual) {
        return repository.existsByEmailAndIdNot(email, idActual);
    }

}

