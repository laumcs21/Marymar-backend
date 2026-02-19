package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;

import java.util.List;

public interface ProductoService {

    ProductoResponseDTO crear(ProductoCreateDTO dto);

    ProductoResponseDTO obtenerPorId(Long id);

    List<ProductoResponseDTO> obtenerTodos();

    ProductoResponseDTO actualizar(Long id, ProductoCreateDTO dto);

    void desactivar(Long id);
}

