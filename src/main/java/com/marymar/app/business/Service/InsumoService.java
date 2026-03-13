package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.InsumoCreateDTO;
import com.marymar.app.business.DTO.InsumoResponseDTO;

import java.util.List;

public interface InsumoService {

    InsumoResponseDTO crear(InsumoCreateDTO dto);

    InsumoResponseDTO obtenerPorId(Long id);

    List<InsumoResponseDTO> obtenerTodos();

    void eliminar(Long id);

    InsumoResponseDTO actualizar(Long id, InsumoCreateDTO dto);
}