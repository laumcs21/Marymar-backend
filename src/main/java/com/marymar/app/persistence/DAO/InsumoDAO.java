package com.marymar.app.persistence.DAO;

import com.marymar.app.business.DTO.InsumoCreateDTO;
import com.marymar.app.business.DTO.InsumoResponseDTO;
import com.marymar.app.persistence.Entity.Insumo;
import com.marymar.app.persistence.Mapper.InsumoMapper;
import com.marymar.app.persistence.Repository.InsumoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class InsumoDAO {

    private final InsumoRepository insumoRepository;
    private final InsumoMapper insumoMapper;

    public InsumoDAO(InsumoRepository insumoRepository, InsumoMapper insumoMapper) {
        this.insumoRepository = insumoRepository;
        this.insumoMapper = insumoMapper;
    }

    public InsumoResponseDTO crear(InsumoCreateDTO dto){

        Insumo insumo = insumoMapper.toEntity(dto);

        insumo = insumoRepository.save(insumo);

        return insumoMapper.toDTO(insumo);
    }

    public InsumoResponseDTO obtenerPorId(Long id){

        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        return insumoMapper.toDTO(insumo);
    }

    public List<InsumoResponseDTO> obtenerTodos(){

        return insumoRepository.findAll()
                .stream()
                .map(insumoMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Insumo obtenerEntidad(Long id){
        return insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
    }

}