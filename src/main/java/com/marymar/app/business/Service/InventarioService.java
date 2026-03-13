package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.business.DTO.InventarioUpdateDTO;

import java.util.List;

public interface InventarioService {

    InventarioResponseDTO crear(InventarioCreateDTO dto);

    InventarioResponseDTO actualizar(Long id, InventarioUpdateDTO dto);

    List<InventarioResponseDTO> obtenerTodos();
    
    void descontarInsumosProducto(Long productoId, Integer cantidadPedido);

    void eliminar(Long id);
}