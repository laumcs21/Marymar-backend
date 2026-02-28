package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;

import java.util.List;

public interface CategoriaService {

    CategoriaResponseDTO crear(CategoriaCreateDTO dto);

    CategoriaResponseDTO obtenerPorId(Long id);

    List<CategoriaResponseDTO> obtenerTodas();

    CategoriaResponseDTO actualizar(Long id, CategoriaCreateDTO dto);

    void eliminar(Long id);

}
