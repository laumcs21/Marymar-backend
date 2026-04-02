package com.marymar.app.business.DTO;

import java.math.BigDecimal;

public class PagoCreateDTO {

    private Long pedidoId;
    private String metodo; // EFECTIVO, TARJETA, TRANSFERENCIA
    private BigDecimal monto;
    private String comprobanteUrl; // opcional (solo transferencia)

    public PagoCreateDTO() {}

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getComprobanteUrl() { return comprobanteUrl; }
    public void setComprobanteUrl(String comprobanteUrl) {
        this.comprobanteUrl = comprobanteUrl;
    }
}