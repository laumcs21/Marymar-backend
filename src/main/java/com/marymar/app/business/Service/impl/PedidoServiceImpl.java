package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.DetallePedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.business.Service.InventarioService;
import com.marymar.app.business.Service.PedidoService;
import com.marymar.app.persistence.DAO.MesaDAO;
import com.marymar.app.persistence.DAO.PedidoDAO;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoDAO pedidoDAO;
    private final PersonaDAO personaDAO;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;
    private final MesaDAO mesaDAO;
    private final DetallePedidoRepository detallePedidoRepository;
    private  final AuditoriaService auditoriaService;


    public PedidoServiceImpl(
            PedidoDAO pedidoDAO,
            PersonaDAO personaDAO,
            ProductoRepository productoRepository,
            InventarioService inventarioService,
            MesaDAO mesaDAO,
            DetallePedidoRepository detallePedidoRepository, AuditoriaService auditoriaService) {

        this.pedidoDAO = pedidoDAO;
        this.personaDAO = personaDAO;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
        this.mesaDAO = mesaDAO;
        this.detallePedidoRepository = detallePedidoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public PedidoResponseDTO crearPedido(PedidoCreateDTO dto) {

        if (dto.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de pedido es obligatorio");
        }

        TipoPedido tipo = TipoPedido.valueOf(dto.getTipo().toUpperCase());

        Persona mesero = null;
        if (dto.getMeseroId() != null) {
            mesero = personaDAO.obtenerEntidadPorId(dto.getMeseroId());
        }

        Pedido pedido;

        if (tipo == TipoPedido.MESA) {

            if (dto.getMesaId() == null) {
                throw new IllegalArgumentException("La mesa es obligatoria para pedidos en mesa");
            }

            Mesa mesa = mesaDAO.obtenerEntidad(dto.getMesaId());
            pedido = new Pedido(mesa, mesero);

        } else {

            if (dto.getClienteId() == null) {
                throw new IllegalArgumentException("El cliente es obligatorio para domicilio");
            }

            Persona cliente = personaDAO.obtenerEntidadPorId(dto.getClienteId());
            pedido = new Pedido(cliente, mesero);
        }

        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }

        for (DetallePedidoCreateDTO d : dto.getDetalles()) {
            Producto producto = productoRepository.findById(d.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            DetallePedido detalle = new DetallePedido(producto, d.getCantidad());
            pedido.agregarDetalle(detalle);
        }
        pedido.calcularTotal();

        auditoriaService.registrar(
                "CREAR_PEDIDO",
                "PEDIDO",
                pedido.getId(),
                "Pedido creado con total: " + pedido.getTotal(),
                null
        );
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
        EstadoPedido estado = EstadoPedido.valueOf(nuevoEstado.toUpperCase());

        pedido.setEstado(estado);

        auditoriaService.registrar(
                "CAMBIAR_ESTADO_PEDIDO",
                "PEDIDO",
                pedido.getId(),
                "Nuevo estado pedido: " + pedido.getEstado(),
                null
        );

        return pedidoDAO.actualizar(pedido);

    }

    @Override
    public PedidoResponseDTO obtenerOCrearPedidoPorMesa(Long mesaId, Long meseroId) {

        Pedido existente = pedidoDAO.obtenerPedidoActivoPorMesa(mesaId);

        if (existente != null) {
            return pedidoDAO.actualizar(existente);
        }

        Persona mesero = personaDAO.obtenerEntidadPorId(meseroId);
        Mesa mesa = mesaDAO.obtenerEntidad(mesaId);

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

    @Override
    public PedidoResponseDTO agregarProducto(Long pedidoId, Long productoId, int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        Pedido pedido = pedidoDAO.obtenerEntidadPorId(pedidoId);

        if (pedido.getEstado() == EstadoPedido.PAGADO) {
            throw new IllegalArgumentException("No se puede modificar un pedido pagado");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));


        DetallePedido detalleExistente = pedido.getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(productoId))
                .findFirst()
                .orElse(null);

        int cantidadTotal = cantidad;

        if (detalleExistente != null) {
            cantidadTotal += detalleExistente.getCantidad();
        }
        inventarioService.validarStockProductoPedido(productoId,cantidadTotal);

        // =========================
        // AGREGAR PRODUCTO AL PEDIDO
        // =========================
        if (detalleExistente != null) {
            detalleExistente.setCantidad(detalleExistente.getCantidad() + cantidad);
        } else {
            DetallePedido detalle = new DetallePedido(producto, cantidad);
            pedido.agregarDetalle(detalle);
        }

        pedido.calcularTotal();
        auditoriaService.registrar(
                "AGREGAR_PRODUCTO",
                "PEDIDO",
                pedido.getId(),
                "Producto: " + producto.getNombre() + " Cantidad: " + cantidad,
                null
        );
        return pedidoDAO.actualizar(pedido);
    }

    @Override
    public PedidoResponseDTO disminuirProducto(Long pedidoId, Long productoId, int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        Pedido pedido = pedidoDAO.obtenerEntidadPorId(pedidoId);

        if (pedido.getEstado() == EstadoPedido.PAGADO) {
            throw new IllegalArgumentException("No se puede modificar un pedido pagado");
        }

        DetallePedido detalleExistente = pedido.getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El producto no está en el pedido"));

        int nuevaCantidad = detalleExistente.getCantidad() - cantidad;

        if (nuevaCantidad <= 0) {
            pedido.getDetalles().remove(detalleExistente);
            detallePedidoRepository.delete(detalleExistente);
        } else {
            detalleExistente.setCantidad(nuevaCantidad);
        }

        pedido.calcularTotal();

        return pedidoDAO.actualizar(pedido);
    }

    @Override
    public PedidoResponseDTO eliminarDetalle(Long detalleId) {

        DetallePedido detalle = detallePedidoRepository.findById(detalleId)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));

        Pedido pedido = detalle.getPedido();

        if (pedido.getEstado() == EstadoPedido.PAGADO) {
            throw new IllegalArgumentException("No se puede modificar un pedido pagado");
        }

        pedido.getDetalles().remove(detalle);
        detallePedidoRepository.delete(detalle);

        pedido.calcularTotal();

        return pedidoDAO.actualizar(pedido);
    }

    @Override
    public PedidoResponseDTO disminuirProducto(Long pedidoId, Long productoId) {

        Pedido pedido = pedidoDAO.obtenerEntidadPorId(pedidoId);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        pedido.agregarOActualizarDetalle(producto, -1);
        auditoriaService.registrar(
                "ELIMINAR_PRODUCTO",
                "PRODUCTO",
                producto.getId(),
                "Producto eliminado: " + producto.getNombre(),
                null
        );
        return pedidoDAO.actualizar(pedido);
    }

    @Override
    public PedidoResponseDTO eliminarDetalle(Long pedidoId, Long detalleId) {

        Pedido pedido = pedidoDAO.obtenerEntidadPorId(pedidoId);

        DetallePedido detalle = pedido.getDetalles().stream()
                .filter(d -> d.getId().equals(detalleId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado"));

        pedido.eliminarDetalle(detalle);

        return pedidoDAO.actualizar(pedido);
    }

    @Override
    public List<PedidoResponseDTO> filtrar(String fechaInicio, String fechaFin, String estado) {

        LocalDateTime inicio = (fechaInicio != null && !fechaInicio.isEmpty())
                ? LocalDateTime.parse(fechaInicio)
                : null;

        LocalDateTime fin = (fechaFin != null && !fechaFin.isEmpty())
                ? LocalDateTime.parse(fechaFin)
                : null;

        EstadoPedido estadoEnum = (estado != null && !estado.isEmpty())
                ? EstadoPedido.valueOf(estado.toUpperCase())
                : null;

        return pedidoDAO.filtrar(inicio, fin, estadoEnum);
    }
}