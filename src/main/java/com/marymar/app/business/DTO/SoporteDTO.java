package com.marymar.app.business.DTO;

import java.time.LocalDateTime;

public class SoporteDTO {

    private Long pagoId;
    private String url;
    private LocalDateTime fecha;
    private Long pedidoId;

    public SoporteDTO(Long pagoId, String url, LocalDateTime fecha, Long pedidoId) {
        this.pagoId = pagoId;
        this.url = url;
        this.fecha = fecha;
        this.pedidoId = pedidoId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Long getPagoId() {
        return pagoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public String getUrl() {
        return url;
    }
}