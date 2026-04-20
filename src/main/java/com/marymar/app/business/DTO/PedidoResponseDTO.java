package com.marymar.app.business.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponseDTO {

    private Long id;
    private LocalDateTime fecha;
    private String estado;

    private String tipo;

    private String clienteNombre;

    private Long meseroId;
    private String meseroNombre;

    private Long mesaId;
    private Integer numeroMesa;

    private BigDecimal total;
    private List<DetallePedidoResponseDTO> detalles;
    private PagoResponseDTO pago;
    private boolean puedeVerSoporte;


    public PedidoResponseDTO() {}

    public PedidoResponseDTO(Long id,
                             LocalDateTime fecha,
                             String estado,
                             String tipo,
                             String clienteNombre,
                             Long meseroId,
                             String meseroNombre,
                             Long mesaId,
                             Integer numeroMesa,
                             BigDecimal total,
                             List<DetallePedidoResponseDTO> detalles,
                             PagoResponseDTO pago) {

        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.tipo = tipo;
        this.clienteNombre = clienteNombre;
        this.meseroId = meseroId;
        this.meseroNombre = meseroNombre;
        this.mesaId = mesaId;
        this.numeroMesa = numeroMesa;
        this.total = total;
        this.detalles = detalles;
        this.pago =pago;
    }

    public Long getId() { return id; }

    public LocalDateTime getFecha() { return fecha; }

    public String getEstado() { return estado; }

    public String getTipo() { return tipo; }

    public String getClienteNombre() { return clienteNombre; }

    public Long getMeseroId() { return meseroId; }

    public String getMeseroNombre() { return meseroNombre; }

    public Long getMesaId() { return mesaId; }

    public Integer getNumeroMesa() { return numeroMesa; }

    public BigDecimal getTotal() { return total; }

    public List<DetallePedidoResponseDTO> getDetalles() { return detalles; }

    public PagoResponseDTO getPago() {return pago;}

    public boolean isPuedeVerSoporte() {
        return puedeVerSoporte;
    }
    public void setPuedeVerSoporte(boolean puedeVerSoporte) {
        this.puedeVerSoporte = puedeVerSoporte;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}