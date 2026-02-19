package com.marymar.app.persistence.DAO;

import com.marymar.app.business.DTO.PedidoResponseDTO;
import com.marymar.app.persistence.Entity.Pedido;
import com.marymar.app.persistence.Mapper.PedidoMapper;
import com.marymar.app.persistence.Repository.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PedidoDAO {

    private final PedidoRepository repository;
    private final PedidoMapper mapper;

    public PedidoDAO(PedidoRepository repository,
                     PedidoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // =========================
    // Guardar pedido (entidad ya armada por Service)
    // =========================
    public PedidoResponseDTO guardar(Pedido pedido) {

        Pedido guardado = repository.save(pedido);
        return mapper.toDTO(guardado);
    }

    // =========================
    // Obtener entidad por ID
    // =========================
    public Pedido obtenerEntidadPorId(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con id " + id));
    }

    // =========================
    // Obtener por ID (DTO)
    // =========================
    public PedidoResponseDTO obtenerPorId(Long id) {

        Pedido pedido = obtenerEntidadPorId(id);
        return mapper.toDTO(pedido);
    }

    // =========================
    // Obtener por cliente
    // =========================
    public List<PedidoResponseDTO> obtenerPorCliente(Long clienteId) {

        return repository.findByClienteId(clienteId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Obtener todos
    // =========================
    public List<PedidoResponseDTO> obtenerTodos() {

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Actualizar estado
    // =========================
    public PedidoResponseDTO actualizar(Pedido pedido) {

        Pedido actualizado = repository.save(pedido);
        return mapper.toDTO(actualizado);
    }
}
