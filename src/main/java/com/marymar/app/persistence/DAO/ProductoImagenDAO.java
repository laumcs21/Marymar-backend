package com.marymar.app.persistence.DAO;

import com.marymar.app.business.DTO.ProductoImagenCreateDTO;
import com.marymar.app.business.DTO.ProductoImagenResponseDTO;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.ProductoImagen;
import com.marymar.app.persistence.Mapper.ProductoImagenMapper;
import com.marymar.app.persistence.Repository.ProductoImagenRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductoImagenDAO {

    private final ProductoImagenRepository repository;
    private final ProductoImagenMapper mapper;

    public ProductoImagenDAO(ProductoImagenRepository repository,
                             ProductoImagenMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // =========================
    // Crear
    // =========================
    public ProductoImagenResponseDTO crear(ProductoImagenCreateDTO dto,
                                           Producto producto,
                                           String url,
                                           String publicId) {

        ProductoImagen imagen = mapper.toEntity(dto, producto);

        imagen.setUrl(url);
        imagen.setPublicId(publicId);

        ProductoImagen guardada = repository.save(imagen);

        return mapper.toDTO(guardada);
    }

    // =========================
    // Obtener por ID
    // =========================
    public ProductoImagenResponseDTO obtenerPorId(Long id) {

        ProductoImagen imagen = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con id " + id));

        return mapper.toDTO(imagen);
    }

    // =========================
    // Obtener por producto
    // =========================
    public List<ProductoImagenResponseDTO> obtenerPorProducto(Long productoId) {

        return repository.findByProductoIdOrderByOrdenAsc(productoId)
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    // =========================
    // Actualizar
    // =========================
    public ProductoImagenResponseDTO actualizar(Long id,
                                                ProductoImagenCreateDTO dto) {

        ProductoImagen existente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con id " + id));

        mapper.updateFromDTO(existente, dto);

        ProductoImagen actualizada = repository.save(existente);

        return mapper.toDTO(actualizada);
    }

    // =========================
    // Eliminar
    // =========================
    public void eliminar(Long id) {

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Imagen no encontrada con id " + id);
        }

        repository.deleteById(id);
    }
}

