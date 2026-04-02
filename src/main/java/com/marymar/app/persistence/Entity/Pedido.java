package com.marymar.app.persistence.Entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = true)
    private Persona cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesero_id")
    private Persona mesero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id", nullable = true)
    private Mesa mesa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPedido tipo;

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pago pago;

    @OneToMany(mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    public Pedido() {}

    public Pedido(Persona cliente, Persona mesero) {
        this.fecha = LocalDateTime.now();
        this.estado = EstadoPedido.CREADO;
        this.tipo = TipoPedido.DOMICILIO;
        this.cliente = cliente;
        this.mesero = mesero;
    }

    public Pedido(Mesa mesa, Persona mesero) {
        this.fecha = LocalDateTime.now();
        this.estado = EstadoPedido.CREADO;
        this.tipo = TipoPedido.MESA;
        this.mesa = mesa;
        this.mesero = mesero;
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
        detalles.add(detalle);
        calcularTotal();
    }

    public void calcularTotal() {
        this.total = detalles.stream()
                .map(d -> d.getSubtotal() != null ? d.getSubtotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public void agregarOActualizarDetalle(Producto producto, int cantidad) {

        if (cantidad == 0) return;

        for (DetallePedido d : detalles) {

            if (d.getProducto().getId().equals(producto.getId())) {

                int nuevaCantidad = d.getCantidad() + cantidad;

                if (nuevaCantidad <= 0) {
                    eliminarDetalle(d);
                } else {
                    d.setCantidad(nuevaCantidad);
                }

                calcularTotal();
                return;
            }
        }

        if (cantidad > 0) {
            DetallePedido nuevo = new DetallePedido(producto, cantidad);
            nuevo.setPedido(this);
            detalles.add(nuevo);
        }

        calcularTotal();
    }

    public void eliminarDetalle(DetallePedido detalle) {
        detalles.remove(detalle);
        detalle.setPedido(null);
        calcularTotal();
    }

    public Long getId() { return id; }

    public LocalDateTime getFecha() { return fecha; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public Persona getCliente() { return cliente; }
    public void setCliente(Persona cliente) { this.cliente = cliente; }

    public Persona getMesero() { return mesero; }
    public void setMesero(Persona mesero) { this.mesero = mesero; }

    public Mesa getMesa() { return mesa; }
    public void setMesa(Mesa mesa) { this.mesa = mesa; }

    public TipoPedido getTipo() { return tipo; }
    public void setTipo(TipoPedido tipo) { this.tipo = tipo; }

    public List<DetallePedido> getDetalles() { return detalles; }

    public BigDecimal getTotal() { return total; }

    public Pago getPago() { return pago; }
    public void setPago(Pago pago) {
        this.pago = pago;
        if (pago != null) {
            pago.setPedido(this);
        }
    }
}