package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.DetallePedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;
import com.marymar.app.business.Service.PedidoService;
import com.marymar.app.persistence.DAO.MesaDAO;
import com.marymar.app.persistence.DAO.PedidoDAO;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Repository.ProductoRepository;
import com.marymar.app.business.Service.InventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoDAO pedidoDAO;
    private final PersonaDAO personaDAO;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;
    private final MesaDAO mesaDAO;

    public PedidoServiceImpl(
            PedidoDAO pedidoDAO,
            PersonaDAO personaDAO,
            ProductoRepository productoRepository,
            InventarioService inventarioService, MesaDAO mesaDAO) {

        this.pedidoDAO = pedidoDAO;
        this.personaDAO = personaDAO;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
        this.mesaDAO = mesaDAO;
    }

    @Override
    public PedidoResponseDTO crearPedido(PedidoCreateDTO dto) {

        if (dto.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de pedido es obligatorio");
        }

        TipoPedido tipo = TipoPedido.valueOf(dto.getTipo());

        Persona mesero = null;
        if (dto.getMeseroId() != null) {
            mesero = personaDAO.obtenerEntidadPorId(dto.getMeseroId());
        }

        Pedido pedido;

        // =========================
        // PEDIDO EN MESA
        // =========================
        if (tipo == TipoPedido.MESA) {

            if (dto.getMesaId() == null) {
                throw new IllegalArgumentException("La mesa es obligatoria para pedidos en mesa");
            }

            Mesa mesa = mesaDAO.obtenerEntidad(dto.getMesaId());

            pedido = new Pedido(mesa, mesero);
        }

        // =========================
        // PEDIDO DOMICILIO
        // =========================
        else {

            if (dto.getClienteId() == null) {
                throw new IllegalArgumentException("El cliente es obligatorio para domicilio");
            }

            Persona cliente = personaDAO.obtenerEntidadPorId(dto.getClienteId());

            pedido = new Pedido(cliente, mesero);
        }

        // =========================
        // DETALLES
        // =========================
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }

        for (DetallePedidoCreateDTO detalleDTO : dto.getDetalles()) {

            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            inventarioService.descontarInsumosProducto(
                    producto.getId(),
                    detalleDTO.getCantidad()
            );

            DetallePedido detalle = new DetallePedido(
                    producto,
                    detalleDTO.getCantidad()
            );

            pedido.agregarDetalle(detalle);
        }

        pedido.calcularTotal();

        return pedidoDAO.guardar(pedido);
    }

    @Override
    public PedidoResponseDTO obtenerPorId(Long id) {
        return pedidoDAO.obtenerPorId(id);
    }

    @Override
    public java.util.List<PedidoResponseDTO> obtenerPorCliente(Long clienteId) {
        return pedidoDAO.obtenerPorCliente(clienteId);
    }

    @Override
    public java.util.List<PedidoResponseDTO> obtenerTodos() {
        return pedidoDAO.obtenerTodos();
    }

    @Override
    public PedidoResponseDTO cambiarEstado(Long id, String nuevoEstado) {

        Pedido pedido = pedidoDAO.obtenerEntidadPorId(id);

        EstadoPedido estado = EstadoPedido.valueOf(nuevoEstado);

        pedido.setEstado(estado);

        return pedidoDAO.actualizar(pedido);
    }

    @Override
    public PedidoResponseDTO obtenerOCrearPedidoPorMesa(Long mesaId, Long meseroId) {

        Pedido existente = pedidoDAO.obtenerPedidoActivoPorMesa(mesaId);

        if (existente != null) {
            return pedidoDAO.actualizar(existente);
        }

        Persona mesero = personaDAO.obtenerEntidadPorId(meseroId);

        Mesa mesa = new Mesa();
        mesa.setId(mesaId);

        Pedido nuevo = new Pedido(mesa, mesero);

        return pedidoDAO.guardar(nuevo);
    }

    @Override
    public PedidoResponseDTO obtenerPedidoPorMesa(Long mesaId) {

        Pedido pedido = pedidoDAO.obtenerPedidoActivoPorMesa(mesaId);

        if (pedido == null) {
            throw new IllegalArgumentException("No hay pedido activo para esta mesa");
        }

        return pedidoDAO.actualizar(pedido);
    }
}