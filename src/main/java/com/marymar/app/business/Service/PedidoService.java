package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.PedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;

import java.util.List;

public interface PedidoService {

    PedidoResponseDTO crearPedido(PedidoCreateDTO dto);

    PedidoResponseDTO obtenerPorId(Long id);

    List<PedidoResponseDTO> obtenerPorCliente(Long clienteId);

    List<PedidoResponseDTO> obtenerTodos();

    PedidoResponseDTO cambiarEstado(Long id, String nuevoEstado);


}