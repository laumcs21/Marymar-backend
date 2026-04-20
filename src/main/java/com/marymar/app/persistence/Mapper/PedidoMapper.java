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

        // =========================
        // 🔥 NUEVA LÓGICA SOPORTE
        // =========================
        boolean puedeVerSoporte = false;

        if (pedido.getPago() != null) {

            String metodo = pedido.getPago().getMetodo().name();
            String comprobante = pedido.getPago().getComprobanteUrl();

            puedeVerSoporte =
                    metodo != null &&
                            metodo.equalsIgnoreCase("TRANSFERENCIA") &&
                            comprobante != null &&
                            !comprobante.isBlank();
        }

        // =========================
        // DTO FINAL
        // =========================
        PedidoResponseDTO dto = new PedidoResponseDTO(
                pedido.getId(),
                pedido.getFecha(),
                pedido.getEstado() != null ? pedido.getEstado().name() : null,
                pedido.getTipo() != null ? pedido.getTipo().name() : null,

                pedido.getCliente() != null ? pedido.getCliente().getNombre() : null,

                pedido.getMesero() != null ? pedido.getMesero().getId() : null,
                pedido.getMesero() != null ? pedido.getMesero().getNombre() : null,

                pedido.getMesa() != null ? pedido.getMesa().getId() : null,
                pedido.getMesa() != null ? pedido.getMesa().getNumero() : null,

                pedido.getTotal(),
                detallesDTO,
                pagoDTO
        );

        // 🔥 SET FINAL
        dto.setPuedeVerSoporte(puedeVerSoporte);

        return dto;
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