package com.marymar.app.business.DTO;

import java.util.List;

public class PedidoCreateDTO {

    private Long clienteId;
    private Long meseroId;
    private Long mesaId;
    private String Tipo;
    private List<DetallePedidoCreateDTO> detalles;

    public PedidoCreateDTO() {}

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getMeseroId() { return meseroId; }
    public void setMeseroId(Long meseroId) { this.meseroId = meseroId; }

    public List<DetallePedidoCreateDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedidoCreateDTO> detalles) {
        this.detalles = detalles;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public String getTipo() {
        return Tipo;
    }

    public void setTipo(String tipo) {
        Tipo = tipo;
    }
}

