package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.persistence.Entity.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    // =========================
    // CreateDTO → Entity
    // =========================
    public Categoria toEntity(CategoriaCreateDTO dto) {

        if (dto == null) {
            return null;
        }

        return new Categoria(dto.getNombre());
    }

    // =========================
    // Entity → ResponseDTO
    // =========================
    public CategoriaResponseDTO toDTO(Categoria categoria) {

        if (categoria == null) {
            return null;
        }

        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNombre(),
0L
        );
    }

    // =========================
    // Update from DTO
    // =========================
    public void updateFromDTO(Categoria categoria, CategoriaCreateDTO dto) {

        if (dto.getNombre() != null) {
            categoria.setNombre(dto.getNombre());
        }
    }
}

