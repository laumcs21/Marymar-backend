package com.marymar.app.persistence.DAO;

import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.persistence.Entity.Insumo;
import com.marymar.app.persistence.Entity.Inventario;
import com.marymar.app.persistence.Mapper.InventarioMapper;
import com.marymar.app.persistence.Repository.InventarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventarioDAO {

    private final InventarioRepository repository;
    private final InventarioMapper mapper;

    public InventarioDAO(InventarioRepository repository, InventarioMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // =========================
    // Crear
    // (Insumo viene ya resuelto desde Service)
    // =========================
    public InventarioResponseDTO crear(InventarioCreateDTO dto, Insumo insumo) {

        Inventario inventario = mapper.toEntity(dto, insumo);
        Inventario guardado = repository.save(inventario);

        return mapper.toDTO(guardado);
    }

    // =========================
    // Obtener entidad por ID
    // =========================
    public Inventario obtenerEntidadPorId(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado con id " + id));
    }

    // =========================
    // Obtener por ID (DTO)
    // =========================
    public InventarioResponseDTO obtenerPorId(Long id) {

        Inventario inventario = obtenerEntidadPorId(id);
        return mapper.toDTO(inventario);
    }

    // =========================
    // Obtener por Insumo (DTO)
    // =========================
    public InventarioResponseDTO obtenerPorInsumoId(Long insumoId) {

        Inventario inventario = repository.findByInsumoId(insumoId)
                .orElseThrow(() -> new EntityNotFoundException("Inventario no encontrado para el insumo " + insumoId));

        return mapper.toDTO(inventario);
    }

    // =========================
    // Obtener todos
    // =========================
    public List<InventarioResponseDTO> obtenerTodos() {

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Actualizar
    // (Insumo viene ya resuelto desde Service)
    // =========================
    public InventarioResponseDTO actualizar(Long id, InventarioCreateDTO dto, Insumo insumo) {

        Inventario existente = obtenerEntidadPorId(id);

        mapper.updateFromDTO(existente, dto, insumo);

        Inventario actualizado = repository.save(existente);

        return mapper.toDTO(actualizado);
    }

    // =========================
    // Eliminar
    // =========================
    public void eliminar(Long id) {

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Inventario no encontrado con id " + id);
        }

        repository.deleteById(id);
    }
}

