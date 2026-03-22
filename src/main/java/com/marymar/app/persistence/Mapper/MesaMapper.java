package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.MesaCreateDTO;
import com.marymar.app.business.DTO.MesaResponseDTO;
import com.marymar.app.persistence.Entity.Mesa;
import org.springframework.stereotype.Component;

@Component
public class MesaMapper {

    public Mesa toEntity(MesaCreateDTO dto) {
        if (dto == null) return null;

        Mesa mesa = new Mesa();
        mesa.setNumero(dto.getNumero());
        mesa.setCapacidad(dto.getCapacidad());
        return mesa;
    }

    public MesaResponseDTO toDTO(Mesa mesa) {
        if (mesa == null) return null;

        MesaResponseDTO dto = new MesaResponseDTO();
        dto.setId(mesa.getId());
        dto.setActiva(mesa.isActiva());
        dto.setNumero(mesa.getNumero());
        dto.setCapacidad(mesa.getCapacidad());
        dto.setEstado(mesa.getEstado());

        if (mesa.getMeseroAsignado() != null) {
            dto.setMeseroAsignadoId(mesa.getMeseroAsignado().getId());
            dto.setMeseroAsignadoNombre(mesa.getMeseroAsignado().getNombre());
        }

        return dto;
    }
}
