package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.persistence.Entity.Insumo;
import com.marymar.app.persistence.Entity.Inventario;
import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {

    // =========================
    // CreateDTO → Entity
    // (Insumo se asigna desde Service/DAO)
    // =========================
    public Inventario toEntity(InventarioCreateDTO dto, Insumo insumo) {

        if (dto == null || insumo == null) {
            return null;
        }

        return new Inventario(insumo, dto.getStock());
    }

    // =========================
    // Entity → ResponseDTO
    // =========================
    public InventarioResponseDTO toDTO(Inventario inventario) {

        if (inventario == null) {
            return null;
        }

        Long insumoId = null;
        String insumoNombre = null;
        String unidad = null;

        if (inventario.getInsumo() != null) {
            insumoId = inventario.getInsumo().getId();
            insumoNombre = inventario.getInsumo().getNombre();
            unidad = inventario.getInsumo().getUnidad();
        }

        return new InventarioResponseDTO(
                inventario.getId(),
                insumoId,
                insumoNombre,
                unidad,
                inventario.getStock(),
                inventario.getFechaCreacion(),
                inventario.getFechaModificacion()
        );
    }

    // =========================
    // Update from DTO
    // =========================
    public void updateFromDTO(Inventario inventario, InventarioCreateDTO dto, Insumo insumo) {

        if (inventario == null || dto == null) {
            return;
        }

        if (insumo != null) {
            inventario.setInsumo(insumo);
        }

        if (dto.getStock() != null) {
            inventario.setStock(dto.getStock());
        }
    }
}

