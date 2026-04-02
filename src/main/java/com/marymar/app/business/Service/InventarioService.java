package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.business.DTO.InventarioUpdateDTO;
import com.marymar.app.persistence.Entity.Pedido;

import java.util.List;

public interface InventarioService {

    InventarioResponseDTO crear(InventarioCreateDTO dto);

    InventarioResponseDTO actualizar(Long id, InventarioUpdateDTO dto);

    List<InventarioResponseDTO> obtenerTodos();
    
    void descontarInsumosProducto(Long productoId, Integer cantidadPedido);

    void eliminar(Long id);

    void validarStockPedido(Pedido pedido);

    void descontarStockPedido(Pedido pedido);

    void validarStockProductoPedido(Long productoId, int cantidad);

    void actualizarDisponibilidadProductos(Long insumoId);
}