package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.DetallePedidoResponseDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;
import com.marymar.app.persistence.Entity.DetallePedido;
import com.marymar.app.persistence.Entity.Pedido;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    // =========================
    // Entity → ResponseDTO
    // =========================
    public PedidoResponseDTO toDTO(Pedido pedido) {

        if (pedido == null) {
            return null;
        }

        List<DetallePedidoResponseDTO> detallesDTO = null;

        if (pedido.getDetalles() != null) {
            detallesDTO = pedido.getDetalles()
                    .stream()
                    .map(this::detalleToDTO)
                    .collect(Collectors.toList());
        }

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getFecha(),
                pedido.getEstado() != null ? pedido.getEstado().name() : null,
                pedido.getCliente() != null ? pedido.getCliente().getNombre() : null,
                pedido.getMesero() != null ? pedido.getMesero().getNombre() : null,
                pedido.getTotal(),
                detallesDTO
        );
    }

    // =========================
    // Detalle Entity → DTO
    // =========================
    private DetallePedidoResponseDTO detalleToDTO(DetallePedido detalle) {

        return new DetallePedidoResponseDTO(
                detalle.getId(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }
}
