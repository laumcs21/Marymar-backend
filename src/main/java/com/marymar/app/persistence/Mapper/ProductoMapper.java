package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.ProductoImagen;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
                categoria,
                dto.getDescripcion()
        );
    }

    // =========================
    // Entity → ResponseDTO
    // =========================
    public ProductoResponseDTO toDTO(Producto producto) {
        if (producto == null) return null;

        List<String> urls = producto.getImagenes() == null ? List.of() :
                producto.getImagenes().stream()
                        .map(ProductoImagen::getUrl)
                        .collect(Collectors.toList());

        String principal = urls.isEmpty() ? null : urls.get(0);

        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getDescripcion(),
                producto.getCategoria() != null ? producto.getCategoria().getId() : null,
                producto.getCategoria() != null ? producto.getCategoria().getNombre() : null,
                producto.isActivo(),
                urls,
                principal
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

        if (dto.getDescripcion() != null) {
            producto.setDescripcion(dto.getDescripcion());
        }
    }
}
