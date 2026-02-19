package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    // =========================
    // CreateDTO → Entity
    // =========================
    public Producto toEntity(ProductoCreateDTO dto, Categoria categoria) {

        if (dto == null) {
            return null;
        }

        return new Producto(
                dto.getNombre(),
                dto.getPrecio(),
                categoria
        );
    }

    // =========================
    // Entity → ResponseDTO
    // =========================
    public ProductoResponseDTO toDTO(Producto producto) {

        if (producto == null) {
            return null;
        }

        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.isActivo()
        );
    }

    // =========================
    // Update from DTO
    // =========================
    public void updateFromDTO(Producto producto, ProductoCreateDTO dto, Categoria categoria) {

        if (dto.getNombre() != null) {
            producto.setNombre(dto.getNombre());
        }

        if (dto.getPrecio() != null) {
            producto.setPrecio(dto.getPrecio());
        }

        if (categoria != null) {
            producto.setCategoria(categoria);
        }
    }
}
