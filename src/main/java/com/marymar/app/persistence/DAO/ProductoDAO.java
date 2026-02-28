package com.marymar.app.persistence.DAO;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Mapper.ProductoMapper;
import com.marymar.app.persistence.Repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoDAO {

    private final ProductoRepository repository;
    private final ProductoMapper mapper;

    public ProductoDAO(ProductoRepository repository, ProductoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // =========================
    // Crear
    // =========================
    public ProductoResponseDTO crear(ProductoCreateDTO dto, Categoria categoria) {

        Producto producto = mapper.toEntity(dto, categoria);
        Producto guardado = repository.save(producto);

        return mapper.toDTO(guardado);
    }

    // =========================
    // Obtener entidad por ID
    // =========================
    public Producto obtenerEntidadPorId(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id " + id));
    }

    // =========================
    // Obtener por ID (DTO)
    // =========================
    public ProductoResponseDTO obtenerPorId(Long id) {

        Producto producto = obtenerEntidadPorId(id);
        return mapper.toDTO(producto);
    }

    // =========================
    // Obtener todos
    // =========================
    public List<ProductoResponseDTO> obtenerTodos() {

        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Actualizar
    // =========================
    public ProductoResponseDTO actualizar(Long id,
                                          ProductoCreateDTO dto,
                                          Categoria categoria) {

        Producto existente = obtenerEntidadPorId(id);

        mapper.updateFromDTO(existente, dto, categoria);

        Producto actualizado = repository.save(existente);

        return mapper.toDTO(actualizado);
    }

    // =========================
    // Desactivar (borrado lógico)
    // =========================
    public void desactivar(Long id) {

        Producto producto = obtenerEntidadPorId(id);
        producto.setActivo(false);
        repository.save(producto);
    }

    public List<ProductoResponseDTO> obtenerPorCategoria(Long categoriaId) {
        return repository.findByCategoriaId(categoriaId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    public Producto guardarEntidad(Producto producto) {
        return repository.save(producto);
    }

    public void eliminarDefinitivo(Long id) {

        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Producto no encontrado");
        }

        repository.deleteById(id);
    }
}


