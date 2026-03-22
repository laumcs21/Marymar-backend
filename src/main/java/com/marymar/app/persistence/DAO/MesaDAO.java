package com.marymar.app.persistence.DAO;

import com.marymar.app.business.DTO.MesaResponseDTO;
import com.marymar.app.persistence.Entity.EstadoMesa;
import com.marymar.app.persistence.Entity.Mesa;
import com.marymar.app.persistence.Mapper.MesaMapper;
import com.marymar.app.persistence.Repository.MesaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MesaDAO {

    private final MesaRepository repository;
    private final MesaMapper mapper;

    public MesaDAO(MesaRepository repository,
                   MesaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // =========================
    // Guardar
    // =========================
    public MesaResponseDTO guardar(Mesa mesa) {

        if (repository.existsByNumero(mesa.getNumero())) {
            throw new IllegalArgumentException("El número de mesa ya está creado");
        }

        Mesa guardada = repository.save(mesa);
        return mapper.toDTO(guardada);
    }

    // =========================
    // Obtener entidad
    // =========================
    public Mesa obtenerEntidad(Long id) {

        Mesa mesa = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));
        return mesa;
    }

    // =========================
    // Obtener DTO
    // =========================
    public MesaResponseDTO obtenerPorId(Long id) {

        return mapper.toDTO(obtenerEntidad(id));
    }

    // =========================
    // Listar
    // =========================
    public List<MesaResponseDTO> listar() {

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Actualizar
    // =========================
    public MesaResponseDTO actualizar(Mesa mesa) {


        if (repository.existsByNumeroAndIdNot(mesa.getNumero(), mesa.getId())) {
            throw new IllegalArgumentException("El número de mesa ya está creado");
        }

        Mesa actualizada = repository.save(mesa);
        return mapper.toDTO(actualizada);
    }

    public void eliminar(Long id) {

        Mesa mesa = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada"));

        if (mesa.getEstado() != EstadoMesa.DISPONIBLE) {
            throw new IllegalArgumentException("No puedes eliminar una mesa en uso");
        }

        repository.deleteById(id);
    }

    public void restaurar(Long id) {
        Mesa mesa = obtenerEntidad(id);
        mesa.setActiva(true);
        repository.save(mesa);
    }
}