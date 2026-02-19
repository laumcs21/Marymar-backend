package com.marymar.app.business.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FacturaResponseDTO {

    private Long id;
    private Long pedidoId;
    private String clienteNombre;
    private LocalDateTime fecha;
    private BigDecimal total;

    public FacturaResponseDTO() {}

    public FacturaResponseDTO(Long id,
                              Long pedidoId,
                              String clienteNombre,
                              LocalDateTime fecha,
                              BigDecimal total) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.clienteNombre = clienteNombre;
        this.fecha = fecha;
        this.total = total;
    }

    public Long getId() { return id; }

    public Long getPedidoId() { return pedidoId; }

    public String getClienteNombre() { return clienteNombre; }

    public LocalDateTime getFecha() { return fecha; }

    public BigDecimal getTotal() { return total; }
}

