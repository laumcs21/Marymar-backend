package com.marymar.app.business.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponseDTO {

    private Long id;
    private LocalDateTime fecha;
    private String estado;
    private String clienteNombre;
    private String meseroNombre;
    private BigDecimal total;
    private List<DetallePedidoResponseDTO> detalles;

    public PedidoResponseDTO() {}

    public PedidoResponseDTO(Long id,
                             LocalDateTime fecha,
                             String estado,
                             String clienteNombre,
                             String meseroNombre,
                             BigDecimal total,
                             List<DetallePedidoResponseDTO> detalles) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.clienteNombre = clienteNombre;
        this.meseroNombre = meseroNombre;
        this.total = total;
        this.detalles = detalles;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public List<DetallePedidoResponseDTO> getDetalles() {
        return detalles;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Long getId() {
        return id;
    }

    public String getMeseroNombre() {
        return meseroNombre;
    }

    public BigDecimal getTotal() {
        return total;
    }
}

