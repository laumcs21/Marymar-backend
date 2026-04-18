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
import com.marymar.app.persistence.Entity.DetallePedido;
import com.marymar.app.persistence.Entity.EstadoMesa;
import com.marymar.app.persistence.Entity.EstadoPedido;
import com.marymar.app.persistence.Entity.Mesa;
import com.marymar.app.persistence.Entity.Pedido;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.TipoPedido;
import com.marymar.app.persistence.Repository.DetallePedidoRepository;
import com.marymar.app.persistence.Repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoDAO pedidoDAO;
    private final PersonaDAO personaDAO;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;
    private final MesaDAO mesaDAO;
    private final DetallePedidoRepository detallePedidoRepository;
    private final AuditoriaService auditoriaService;

    public PedidoServiceImpl(
            PedidoDAO pedidoDAO,
            PersonaDAO personaDAO,
            ProductoRepository productoRepository,
            InventarioService inventarioService,
            MesaDAO mesaDAO,
            DetallePedidoRepository detallePedidoRepository,
            AuditoriaService auditoriaService
    ) {
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
        validarSolicitudCreacion(dto);

        TipoPedido tipo = parseTipo(dto.getTipo());
        Persona mesero = dto.getMeseroId() != null
                ? personaDAO.obtenerEntidadPorId(dto.getMeseroId())
                : null;

        Pedido pedido = construirPedido(dto, tipo, mesero);

        for (DetallePedidoCreateDTO detalleDto : dto.getDetalles()) {
            if (detalleDto.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a 0");
            }

            Producto producto = productoRepository.findById(detalleDto.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (!producto.isActivo()) {
                throw new IllegalArgumentException(
                        "El producto " + producto.getNombre() + " no está disponible en este momento"
                );
            }

            DetallePedido detalle = new DetallePedido(producto, detalleDto.getCantidad());
            pedido.agregarDetalle(detalle);
        }

        pedido.calcularTotal();
        inventarioService.validarStockPedido(pedido);

        PedidoResponseDTO guardado = pedidoDAO.guardar(pedido);

        auditoriaService.registrar(
                "CREAR_PEDIDO",
                "PEDIDO",
                guardado.getId(),
                "Pedido " + guardado.getTipo() + " creado con total: " + guardado.getTotal(),
                null
        );

        return guardado;
    }

    @Override
    public PedidoResponseDTO obtenerPorId(Long id) {
        return pedidoDAO.obtenerPorId(id);
    }

    @Override
    public List<PedidoResponseDTO> obtenerPorCliente(Long clienteId) {
        return pedidoDAO.obtenerPorCliente(clienteId);
    }

    @Override
    public Pedido obtenerEntidad(Long id) {
        return pedidoDAO.obtenerEntidadPorId(id);
    }

    @Override
    public void guardarEntidad(Pedido pedido) {
        pedidoDAO.actualizar(pedido);
    }

    @Override
    public List<PedidoResponseDTO> obtenerTodos() {
        return pedidoDAO.obtenerTodos();
    }

    @Override
    public PedidoResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoDAO.obtenerEntidadPorId(id);
        EstadoPedido estadoObjetivo = parseEstado(nuevoEstado);
        EstadoPedido estadoActual = pedido.getEstado();

        if (!transicionPermitida(pedido, estadoActual, estadoObjetivo)) {
            throw new IllegalArgumentException("Cambio de estado no permitido");
        }

        pedido.setEstado(estadoObjetivo);
        sincronizarMesaSegunEstado(pedido, estadoObjetivo);

        auditoriaService.registrar(
                "CAMBIAR_ESTADO_PEDIDO",
                "PEDIDO",
                pedido.getId(),
                "Estado actualizado de " + estadoActual + " a " + estadoObjetivo,
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

        if (!mesa.isActiva()) {
            throw new IllegalArgumentException("La mesa está inactiva");
        }

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
        validarPedidoModificable(pedido);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (!producto.isActivo()) {
            throw new IllegalArgumentException("El producto seleccionado no está disponible");
        }

        DetallePedido detalleExistente = pedido.getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(productoId))
                .findFirst()
                .orElse(null);

        int cantidadTotal = cantidad + (detalleExistente != null ? detalleExistente.getCantidad() : 0);
        inventarioService.validarStockProductoPedido(productoId, cantidadTotal);

        if (detalleExistente != null) {
            detalleExistente.setCantidad(detalleExistente.getCantidad() + cantidad);
        } else {
            pedido.agregarDetalle(new DetallePedido(producto, cantidad));
        }

        pedido.calcularTotal();

        auditoriaService.registrar(
                "AGREGAR_PRODUCTO",
                "PEDIDO",
                pedido.getId(),
                "Producto: " + producto.getNombre() + " | Cantidad agregada: " + cantidad,
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
        validarPedidoModificable(pedido);

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
        validarPedidoModificable(pedido);

        pedido.getDetalles().remove(detalle);
        detallePedidoRepository.delete(detalle);
        pedido.calcularTotal();

        return pedidoDAO.actualizar(pedido);
    }

    @Override
    public PedidoResponseDTO disminuirProducto(Long pedidoId, Long productoId) {
        Pedido pedido = pedidoDAO.obtenerEntidadPorId(pedidoId);
        validarPedidoModificable(pedido);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        pedido.agregarOActualizarDetalle(producto, -1);

        auditoriaService.registrar(
                "DISMINUIR_PRODUCTO",
                "PEDIDO",
                pedido.getId(),
                "Producto disminuido: " + producto.getNombre(),
                null
        );

        return pedidoDAO.actualizar(pedido);
    }

    @Override
    public PedidoResponseDTO eliminarDetalle(Long pedidoId, Long detalleId) {
        Pedido pedido = pedidoDAO.obtenerEntidadPorId(pedidoId);
        validarPedidoModificable(pedido);

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
                ? EstadoPedido.valueOf(estado.toUpperCase(Locale.ROOT))
                : null;

        return pedidoDAO.filtrar(inicio, fin, estadoEnum);
    }

    @Override
    public List<PedidoResponseDTO> obtenerColaCocina(String estado) {
        EstadoPedido estadoEnum = EstadoPedido.valueOf(estado.toUpperCase(Locale.ROOT));
        return pedidoDAO.obtenerCola(estadoEnum);
    }

    private void validarSolicitudCreacion(PedidoCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La solicitud del pedido es obligatoria");
        }
        if (dto.getTipo() == null || dto.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de pedido es obligatorio");
        }
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        }
    }

    private TipoPedido parseTipo(String tipo) {
        try {
            return TipoPedido.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("Tipo de pedido inválido");
        }
    }

    private EstadoPedido parseEstado(String estado) {
        try {
            return EstadoPedido.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new IllegalArgumentException("Estado de pedido inválido");
        }
    }

    private Pedido construirPedido(PedidoCreateDTO dto, TipoPedido tipo, Persona mesero) {
        if (tipo == TipoPedido.MESA) {
            if (dto.getMesaId() == null) {
                throw new IllegalArgumentException("La mesa es obligatoria para pedidos en mesa");
            }

            if (pedidoDAO.obtenerPedidoActivoPorMesa(dto.getMesaId()) != null) {
                throw new IllegalArgumentException("La mesa ya tiene un pedido activo. Usa el flujo de edición de mesa");
            }

            Mesa mesa = mesaDAO.obtenerEntidad(dto.getMesaId());
            if (!mesa.isActiva()) {
                throw new IllegalArgumentException("La mesa seleccionada está inactiva");
            }
            return new Pedido(mesa, mesero);
        }

        if (dto.getClienteId() == null) {
            throw new IllegalArgumentException("El cliente es obligatorio para domicilio");
        }

        Persona cliente = personaDAO.obtenerEntidadPorId(dto.getClienteId());
        return new Pedido(cliente, mesero);
    }

    private void validarPedidoModificable(Pedido pedido) {
        if (pedido.getEstado() == EstadoPedido.PAGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException("No se puede modificar un pedido finalizado");
        }
    }

    private boolean transicionPermitida(Pedido pedido, EstadoPedido actual, EstadoPedido objetivo) {
        if (actual == objetivo) {
            return true;
        }

        if (objetivo == EstadoPedido.CANCELADO) {
            return actual == EstadoPedido.CREADO ||
                    actual == EstadoPedido.CONFIRMADO ||
                    actual == EstadoPedido.EN_PREPARACION ||
                    actual == EstadoPedido.LISTO;
        }

        if (pedido.getTipo() == TipoPedido.DOMICILIO) {
            return switch (actual) {
                case CREADO, CONFIRMADO -> objetivo == EstadoPedido.EN_PREPARACION;
                case EN_PREPARACION -> objetivo == EstadoPedido.LISTO;
                case LISTO -> objetivo == EstadoPedido.ENTREGADO;
                default -> false;
            };
        }

        return switch (actual) {
            case CREADO, CONFIRMADO -> objetivo == EstadoPedido.EN_PREPARACION;
            case EN_PREPARACION -> objetivo == EstadoPedido.LISTO;
            case LISTO -> objetivo == EstadoPedido.ENTREGADO;
            case ENTREGADO -> objetivo == EstadoPedido.CUENTA_PEDIDA;
            case CUENTA_PEDIDA -> objetivo == EstadoPedido.PAGADO;
            default -> false;
        };
    }

    private void sincronizarMesaSegunEstado(Pedido pedido, EstadoPedido nuevoEstado) {
        if (pedido.getMesa() == null) {
            return;
        }

        Mesa mesa = pedido.getMesa();
        if (nuevoEstado == EstadoPedido.CUENTA_PEDIDA) {
            mesa.setEstado(EstadoMesa.CUENTA_PEDIDA);
            mesaDAO.actualizar(mesa);
        } else if (nuevoEstado == EstadoPedido.CANCELADO) {
            mesa.setEstado(EstadoMesa.DISPONIBLE);
            mesa.setMeseroAsignado(null);
            mesaDAO.actualizar(mesa);
        }
    }
}