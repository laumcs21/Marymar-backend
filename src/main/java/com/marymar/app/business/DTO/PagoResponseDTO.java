package com.marymar.app.business.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoResponseDTO {

    private Long id;
    private String metodo;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private String comprobanteUrl;

    public PagoResponseDTO() {}

    public PagoResponseDTO(Long id, String metodo, BigDecimal monto,
                           LocalDateTime fechaPago, String comprobanteUrl) {

        this.id = id;
        this.metodo = metodo;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.comprobanteUrl = comprobanteUrl;
    }

    public Long getId() { return id; }
    public String getMetodo() { return metodo; }
    public BigDecimal getMonto() { return monto; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public String getComprobanteUrl() { return comprobanteUrl; }
}