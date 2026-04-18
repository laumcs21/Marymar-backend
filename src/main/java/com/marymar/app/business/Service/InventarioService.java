package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.business.DTO.InventarioUpdateDTO;
import com.marymar.app.persistence.Entity.Pedido;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    void registrarEntradaLote(Long insumoId, int cantidad, LocalDateTime fechaVencimiento);

    void descontarDeLotes(Long insumoId, int cantidadNecesaria);

    @Transactional
    void ingresarStock(Long insumoId, int cantidad, LocalDateTime fechaVencimiento);

    @Transactional
    void surtirCocina(Long insumoId, int cantidad);

    int obtenerStockCocina(Long insumoId);
}