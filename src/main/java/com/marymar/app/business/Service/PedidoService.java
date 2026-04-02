package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.PedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface PedidoService {

    PedidoResponseDTO crearPedido(PedidoCreateDTO dto);

    PedidoResponseDTO obtenerPorId(Long id);

    List<PedidoResponseDTO> obtenerPorCliente(Long clienteId);

    List<PedidoResponseDTO> obtenerTodos();

    PedidoResponseDTO cambiarEstado(Long id, String nuevoEstado);


    PedidoResponseDTO obtenerOCrearPedidoPorMesa(Long mesaId, Long meseroId);

    PedidoResponseDTO obtenerPedidoPorMesa(Long mesaId);

    PedidoResponseDTO agregarProducto(Long pedidoId, Long productoId, int cantidad);

    PedidoResponseDTO disminuirProducto(Long pedidoId, Long productoId, int cantidad);

    PedidoResponseDTO eliminarDetalle(Long detalleId);

    PedidoResponseDTO disminuirProducto(Long pedidoId, Long productoId);

    PedidoResponseDTO eliminarDetalle(Long pedidoId, Long detalleId);
}