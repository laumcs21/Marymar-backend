package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.DetallePedidoCreateDTO;
import com.marymar.app.business.DTO.DetallePedidoResponseDTO;
import com.marymar.app.persistence.Entity.DetallePedido;
import com.marymar.app.persistence.Entity.Producto;
import org.springframework.stereotype.Component;

@Component
public class DetallePedidoMapper {

    // =========================
    // CreateDTO → Entity
    // =========================
    public DetallePedido toEntity(DetallePedidoCreateDTO dto, Producto producto) {

        if (dto == null || producto == null) {
            return null;
        }

        return new DetallePedido(producto, dto.getCantidad());
    }

    // =========================
    // Entity → ResponseDTO
    // =========================
    public DetallePedidoResponseDTO toDTO(DetallePedido detalle) {

        if (detalle == null) {
            return null;
        }

        return new DetallePedidoResponseDTO(
                detalle.getId(),
                detalle.getProducto() != null ? detalle.getProducto().getNombre() : null,
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }

    // =========================
    // Update from DTO
    // =========================
    public void updateFromDTO(DetallePedido detalle,
                              DetallePedidoCreateDTO dto,
                              Producto producto) {

        if (producto != null) {
            detalle.setProducto(producto);
            detalle.setPrecioUnitario(producto.getPrecio());
        }

        if (dto.getCantidad() != null) {
            detalle.setCantidad(dto.getCantidad());
        }

        // recalcular subtotal
        if (detalle.getPrecioUnitario() != null && detalle.getCantidad() != null) {
            detalle.setSubtotal(
                    detalle.getPrecioUnitario()
                            .multiply(java.math.BigDecimal.valueOf(detalle.getCantidad()))
            );
        }
    }
}

