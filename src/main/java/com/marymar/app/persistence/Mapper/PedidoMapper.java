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

    private final PagoMapper pagoMapper;

    public PedidoMapper(PagoMapper pagoMapper) {
        this.pagoMapper = pagoMapper;
    }

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

        var pagoDTO = pagoMapper.toDTO(pedido.getPago());
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getFecha(),
                pedido.getEstado() != null ? pedido.getEstado().name() : null,
                pedido.getTipo() != null ? pedido.getTipo().name() : null, // 🔥 NUEVO

                // cliente (solo si existe)
                pedido.getCliente() != null ? pedido.getCliente().getNombre() : null,

                // mesero
                pedido.getMesero() != null ? pedido.getMesero().getId() : null,
                pedido.getMesero() != null ? pedido.getMesero().getNombre() : null,

                // mesa (solo si existe)
                pedido.getMesa() != null ? pedido.getMesa().getId() : null,
                pedido.getMesa() != null ? pedido.getMesa().getNumero() : null,

                pedido.getTotal(),
                detallesDTO,
                pagoDTO
        );
    }

    private DetallePedidoResponseDTO detalleToDTO(DetallePedido detalle) {

        return new DetallePedidoResponseDTO(
                detalle.getId(),
                detalle.getProducto().getNombre(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal(),
                detalle.getProducto().getId()
        );
    }
}