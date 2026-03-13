package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.DetallePedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;
import com.marymar.app.business.Service.PedidoService;
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

    public PedidoServiceImpl(
            PedidoDAO pedidoDAO,
            PersonaDAO personaDAO,
            ProductoRepository productoRepository,
            InventarioService inventarioService) {

        this.pedidoDAO = pedidoDAO;
        this.personaDAO = personaDAO;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
    }

    @Override
    public PedidoResponseDTO crearPedido(PedidoCreateDTO dto) {

        Persona cliente = personaDAO.obtenerEntidadPorId(dto.getClienteId());

        Persona mesero = null;
        if (dto.getMeseroId() != null) {
            mesero = personaDAO.obtenerEntidadPorId(dto.getMeseroId());
        }

        Pedido pedido = new Pedido(cliente, mesero);

        if(dto.getDetalles() == null || dto.getDetalles().isEmpty()){
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
}