package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.PagoCreateDTO;
import com.marymar.app.business.DTO.PagoResponseDTO;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.business.Service.ImageService;
import com.marymar.app.business.Service.InventarioService;
import com.marymar.app.business.Service.PagoService;
import com.marymar.app.persistence.DAO.MesaDAO;
import com.marymar.app.persistence.DAO.PedidoDAO;
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Mapper.PagoMapper;
import com.marymar.app.persistence.Repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoDAO pedidoDAO;
    private final MesaDAO mesaDAO;
    private final PagoMapper pagoMapper;
    private final ImageService imageService;
    private final InventarioService inventarioService;
    private final AuditoriaService auditoriaService;
    public PagoServiceImpl(PagoRepository pagoRepository,
                           PedidoDAO pedidoDAO,
                           MesaDAO mesaDAO,
                           PagoMapper pagoMapper,
                           ImageService imageService,
                           InventarioService inventarioService, AuditoriaService auditoriaService) {

        this.pagoRepository = pagoRepository;
        this.pedidoDAO = pedidoDAO;
        this.mesaDAO = mesaDAO;
        this.pagoMapper = pagoMapper;
        this.imageService = imageService;
        this.inventarioService = inventarioService;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public PagoResponseDTO registrarPago(PagoCreateDTO dto, MultipartFile comprobante) {

        // =========================
        // VALIDACIONES BÁSICAS
        // =========================
        if (dto.getPedidoId() == null) {
            throw new IllegalArgumentException("El pedido es obligatorio");
        }

        if (dto.getMetodo() == null || dto.getMetodo().isBlank()) {
            throw new IllegalArgumentException("El método de pago es obligatorio");
        }

        if (dto.getMonto() == null) {
            throw new IllegalArgumentException("El monto es obligatorio");
        }

        Pedido pedido = pedidoDAO.obtenerEntidadPorId(dto.getPedidoId());

        if (pedido.getEstado() == EstadoPedido.PAGADO) {
            throw new IllegalArgumentException("El pedido ya está pagado");
        }

        if (pedido.getEstado() != EstadoPedido.CUENTA_PEDIDA) {
            throw new IllegalArgumentException("Primero debes generar la factura");
        }

        MetodoPago metodo;
        try {
            metodo = MetodoPago.valueOf(dto.getMetodo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Método de pago inválido");
        }

        if (dto.getMonto().compareTo(pedido.getTotal()) != 0) {
            throw new IllegalArgumentException("El monto no coincide con el total del pedido");
        }

        // =========================
        // MANEJO COMPROBANTE
        // =========================
        String urlComprobante = null;

        if (metodo == MetodoPago.TRANSFERENCIA) {

            if (pedido.getTipo() == TipoPedido.MESA) {

                if (comprobante == null || comprobante.isEmpty()) {
                    throw new IllegalArgumentException("Debe adjuntar el comprobante");
                }

                try {
                    var upload = imageService.uploadImage(
                            comprobante,
                            "pagos",
                            "pago_" + pedido.getId()
                    );

                    urlComprobante = upload.getUrl();

                } catch (Exception e) {
                    throw new RuntimeException("Error al subir comprobante", e);
                }

            } else {
                throw new IllegalArgumentException("Transferencia no permitida en domicilio");
            }
        }

        // =========================
        // CREAR PAGO
        // =========================
        Pago pago = new Pago(pedido, metodo, dto.getMonto());
        pago.setComprobanteUrl(urlComprobante);
        pedido.setPago(pago);

        // =========================
        // INVENTARIO
        // =========================

        inventarioService.validarStockPedido(pedido);
        pagoRepository.save(pago);
        inventarioService.descontarStockPedido(pedido);

        // =========================
        // FINALIZAR PEDIDO
        // =========================
        pedido.setEstado(EstadoPedido.PAGADO);
        pedidoDAO.actualizar(pedido);

        // =========================
        // LIBERAR MESA
        // =========================
        if (pedido.getMesa() != null) {
            Mesa mesa = pedido.getMesa();
            mesa.setEstado(EstadoMesa.DISPONIBLE);
            mesa.setMeseroAsignado(null);
            mesaDAO.actualizar(mesa);
        }
        auditoriaService.registrar(
                "PAGO",
                "PEDIDO",
                pedido.getId(),
                "Pago realizado: $" + pedido.getTotal(),
                null
        );
        return pagoMapper.toDTO(pago);
    }

    @Override
    public PagoResponseDTO obtenerPorPedido(Long pedidoId) {

        Pago pago = pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("No existe pago para este pedido"));

        return pagoMapper.toDTO(pago);
    }

    @Override
    public List<String> obtenerSoportes(Long pedidoId, LocalDateTime inicio, LocalDateTime fin) {

        List<Pago> pagos;

        if (inicio != null && fin != null) {
            pagos = pagoRepository
                    .findByPedidoIdAndComprobanteUrlIsNotNullAndFechaPagoBetween(
                            pedidoId, inicio, fin
                    );
        } else {
            pagos = pagoRepository
                    .findByPedidoIdAndComprobanteUrlIsNotNull(pedidoId);
        }

        return pagos.stream()
                .map(Pago::getComprobanteUrl)
                .toList();
    }
}