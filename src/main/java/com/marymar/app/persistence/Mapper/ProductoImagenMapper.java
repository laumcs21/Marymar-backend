package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.ProductoImagenCreateDTO;
import com.marymar.app.business.DTO.ProductoImagenResponseDTO;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.ProductoImagen;
import org.springframework.stereotype.Component;

@Component
public class ProductoImagenMapper {

    // =========================
    // CreateDTO -> Entity
    // =========================
    public ProductoImagen toEntity(ProductoImagenCreateDTO dto, Producto producto) {

        if (dto == null) return null;

        ProductoImagen imagen = new ProductoImagen();

        imagen.setProducto(producto);
        imagen.setOrden(dto.getOrden() != null ? dto.getOrden() : 1);
        imagen.setPrincipal(dto.getPrincipal() != null ? dto.getPrincipal() : false);

        return imagen;
    }

    // =========================
    // Entity -> ResponseDTO
    // =========================
    public ProductoImagenResponseDTO toDTO(ProductoImagen entity) {

        if (entity == null) return null;

        ProductoImagenResponseDTO dto = new ProductoImagenResponseDTO();

        dto.setId(entity.getId());
        dto.setUrl(entity.getUrl());
        dto.setOrden(entity.getOrden());
        dto.setPrincipal(entity.getPrincipal());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }

    // =========================
    // Update
    // =========================
    public void updateFromDTO(ProductoImagen entity, ProductoImagenCreateDTO dto) {

        if (dto == null || entity == null) return;

        if (dto.getOrden() != null) {
            entity.setOrden(dto.getOrden());
        }

        if (dto.getPrincipal() != null) {
            entity.setPrincipal(dto.getPrincipal());
        }
    }
}
