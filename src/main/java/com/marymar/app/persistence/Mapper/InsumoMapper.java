package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.InsumoCreateDTO;
import com.marymar.app.business.DTO.InsumoResponseDTO;
import com.marymar.app.persistence.Entity.Insumo;
import org.springframework.stereotype.Component;

@Component
public class InsumoMapper {

    public Insumo toEntity(InsumoCreateDTO dto) {

        if(dto == null){
            return null;
        }

        return new Insumo(
                dto.getNombre(),
                dto.getUnidad()
        );
    }

    public InsumoResponseDTO toDTO(Insumo insumo){

        if(insumo == null){
            return null;
        }

        return new InsumoResponseDTO(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidad()
        );
    }
}