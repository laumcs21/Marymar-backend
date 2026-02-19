package com.marymar.app.persistence.DAO;

import com.marymar.app.business.DTO.DetallePedidoCreateDTO;
import com.marymar.app.business.DTO.DetallePedidoResponseDTO;
import com.marymar.app.persistence.Entity.DetallePedido;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Mapper.DetallePedidoMapper;
import com.marymar.app.persistence.Repository.DetallePedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DetallePedidoDAO {

    private final DetallePedidoRepository repository;
    private final DetallePedidoMapper mapper;

    public DetallePedidoDAO(DetallePedidoRepository repository,
                            DetallePedidoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // =========================
    // Crear
    // =========================
    public DetallePedidoResponseDTO crear(DetallePedidoCreateDTO dto,
                                          Producto producto,
                                          com.marymar.app.persistence.Entity.Pedido pedido) {

        DetallePedido detalle = mapper.toEntity(dto, producto);
        detalle.setPedido(pedido);

        DetallePedido guardado = repository.save(detalle);

        return mapper.toDTO(guardado);
    }

    // =========================
    // Obtener entidad por ID
    // =========================
    public DetallePedido obtenerEntidadPorId(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle no encontrado con id " + id));
    }

    // =========================
    // Obtener por ID (DTO)
    // =========================
    public DetallePedidoResponseDTO obtenerPorId(Long id) {

        DetallePedido detalle = obtenerEntidadPorId(id);
        return mapper.toDTO(detalle);
    }

    // =========================
    // Obtener por pedido
    // =========================
    public List<DetallePedidoResponseDTO> obtenerPorPedido(Integer pedidoId) {

        return repository.findByPedidoId(pedidoId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Actualizar
    // =========================
    public DetallePedidoResponseDTO actualizar(Long id,
                                               DetallePedidoCreateDTO dto,
                                               Producto producto) {

        DetallePedido existente = obtenerEntidadPorId(id);

        mapper.updateFromDTO(existente, dto, producto);

        DetallePedido actualizado = repository.save(existente);

        return mapper.toDTO(actualizado);
    }

    // =========================
    // Eliminar
    // =========================
    public void eliminar(Long id) {

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Detalle no encontrado con id " + id);
        }

        repository.deleteById(id);
    }
}
