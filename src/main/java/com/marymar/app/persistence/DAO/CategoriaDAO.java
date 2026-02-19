package com.marymar.app.persistence.DAO;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Mapper.CategoriaMapper;
import com.marymar.app.persistence.Repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoriaDAO {

    private final CategoriaRepository repository;
    private final CategoriaMapper mapper;

    public CategoriaDAO(CategoriaRepository repository, CategoriaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // =========================
    // Crear
    // =========================
    public CategoriaResponseDTO crear(CategoriaCreateDTO dto) {

        Categoria categoria = mapper.toEntity(dto);
        Categoria guardada = repository.save(categoria);

        return mapper.toDTO(guardada);
    }

    // =========================
    // Obtener entidad por ID
    // =========================
    public Categoria obtenerEntidadPorId(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria no encontrada con id " + id));
    }

    // =========================
    // Obtener por ID (DTO)
    // =========================
    public CategoriaResponseDTO obtenerPorId(Long id) {

        Categoria categoria = obtenerEntidadPorId(id);
        return mapper.toDTO(categoria);
    }

    // =========================
    // Obtener todas
    // =========================
    public List<CategoriaResponseDTO> obtenerTodas() {

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Actualizar
    // =========================
    public CategoriaResponseDTO actualizar(Long id, CategoriaCreateDTO dto) {

        Categoria existente = obtenerEntidadPorId(id);

        mapper.updateFromDTO(existente, dto);

        Categoria actualizada = repository.save(existente);

        return mapper.toDTO(actualizada);
    }

    // =========================
    // Eliminar
    // =========================
    public void eliminar(Long id) {

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Categoria no encontrada con id " + id);
        }

        repository.deleteById(id);
    }

    public boolean existePorNombre(String nombre) {
        return repository.findByNombre(nombre).isPresent();
    }

}

