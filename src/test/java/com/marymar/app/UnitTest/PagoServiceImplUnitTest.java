package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.PagoCreateDTO;
import com.marymar.app.business.DTO.PagoResponseDTO;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.business.Service.ImageService;
import com.marymar.app.business.Service.InventarioService;
import com.marymar.app.business.Service.impl.PagoServiceImpl;
import com.marymar.app.persistence.DAO.MesaDAO;
import com.marymar.app.persistence.DAO.PedidoDAO;
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Mapper.PagoMapper;
import com.marymar.app.persistence.Repository.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplUnitTest {

    @Mock private PagoRepository pagoRepository;
    @Mock private PedidoDAO pedidoDAO;
    @Mock private MesaDAO mesaDAO;
    @Mock private PagoMapper pagoMapper;
    @Mock private ImageService imageService;
    @Mock private InventarioService inventarioService;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks private PagoServiceImpl service;

    private Pedido pedidoMesa;
    private Pedido pedidoDomicilio;
    private PagoCreateDTO dto;

    @BeforeEach
    void setUp() {
        Categoria categoria = TestDataFactory.categoria(1L, "Especiales");
        Producto producto = TestDataFactory.producto(5L, "Mojarra", new BigDecimal("25000"), categoria);
        Mesa mesa = TestDataFactory.mesa(3L, 8, 4);
        Persona mesero = TestDataFactory.persona(2L, "Mesero", "mesero@test.com", Rol.MESERO);
        Persona cliente = TestDataFactory.persona(4L, "Cliente", "cliente@test.com", Rol.CLIENTE);

        pedidoMesa = TestDataFactory.pedidoMesa(10L, mesa, mesero, producto, 1);
        pedidoDomicilio = TestDataFactory.pedidoDomicilio(11L, cliente, mesero, producto, 1);

        dto = new PagoCreateDTO();
        dto.setPedidoId(10L);
        dto.setMetodo("EFECTIVO");
        dto.setMonto(new BigDecimal("25000"));
    }

    // ============================
    // VALIDACIONES
    // ============================

    @Test
    void registrarPagoDeberiaFallarSiPedidoEsObligatorio() {
        dto.setPedidoId(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPago(dto, null)
        );

        assertEquals("El pedido es obligatorio", ex.getMessage());
    }

    @Test
    void registrarPagoDeberiaFallarSiNoEstaEnCuentaPedida() {
        pedidoMesa.setEstado(EstadoPedido.CREADO);
        when(pedidoDAO.obtenerEntidadPorId(10L)).thenReturn(pedidoMesa);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPago(dto, null)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("factura"));
    }

    @Test
    void registrarPagoDeberiaFallarSiMontoNoCoincide() {
        pedidoMesa.setEstado(EstadoPedido.CUENTA_PEDIDA);
        dto.setMonto(new BigDecimal("20000"));

        when(pedidoDAO.obtenerEntidadPorId(10L)).thenReturn(pedidoMesa);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPago(dto, null)
        );

        assertEquals("El monto no coincide con el total del pedido", ex.getMessage());
    }

    @Test
    void registrarPagoDeberiaFallarSiYaEstaPagado() {
        pedidoMesa.setEstado(EstadoPedido.PAGADO);
        when(pedidoDAO.obtenerEntidadPorId(10L)).thenReturn(pedidoMesa);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPago(dto, null)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("pagado"));
    }

    // ============================
    // CASO FELIZ EFECTIVO
    // ============================

    @Test
    void registrarPagoEfectivoDeberiaValidarStockGuardarPagoYPagarPedido() {
        pedidoMesa.setEstado(EstadoPedido.CUENTA_PEDIDA);

        Pago pago = TestDataFactory.pago(1L, pedidoMesa, MetodoPago.EFECTIVO, new BigDecimal("25000"));

        PagoResponseDTO response = new PagoResponseDTO(
                1L,
                "EFECTIVO",
                new BigDecimal("25000"),
                LocalDateTime.now(),
                null
        );

        when(pedidoDAO.obtenerEntidadPorId(10L)).thenReturn(pedidoMesa);
        when(pagoRepository.save(any(Pago.class))).thenReturn(pago);
        when(pagoMapper.toDTO(any(Pago.class))).thenReturn(response);

        PagoResponseDTO resultado = service.registrarPago(dto, null);

        assertSame(response, resultado);

        assertEquals(EstadoPedido.PAGADO, pedidoMesa.getEstado());
        assertEquals(EstadoMesa.DISPONIBLE, pedidoMesa.getMesa().getEstado());
        assertNull(pedidoMesa.getMesa().getMeseroAsignado());

        verify(inventarioService).validarStockPedido(pedidoMesa);
        verify(inventarioService).descontarStockPedido(pedidoMesa);
        verify(mesaDAO).actualizar(pedidoMesa.getMesa());

        verify(auditoriaService).registrar(
                eq("PAGO"),
                eq("PEDIDO"),
                eq(10L),
                contains("25000"),
                isNull()
        );
    }

    // ============================
    // TRANSFERENCIA
    // ============================

    @Test
    void registrarPagoTransferenciaMesaDeberiaExigirComprobante() {
        pedidoMesa.setEstado(EstadoPedido.CUENTA_PEDIDA);
        dto.setMetodo("TRANSFERENCIA");

        when(pedidoDAO.obtenerEntidadPorId(10L)).thenReturn(pedidoMesa);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPago(dto, null)
        );

        assertEquals("Debe adjuntar el comprobante", ex.getMessage());
    }

    @Test
    void registrarPagoTransferenciaMesaDeberiaSubirComprobante() throws Exception {
        pedidoMesa.setEstado(EstadoPedido.CUENTA_PEDIDA);
        dto.setMetodo("TRANSFERENCIA");

        MultipartFile comprobante = new MockMultipartFile(
                "comprobante",
                "pago.jpg",
                "image/jpeg",
                "img".getBytes()
        );

        when(pedidoDAO.obtenerEntidadPorId(10L)).thenReturn(pedidoMesa);

        when(imageService.uploadImage(eq(comprobante), eq("pagos"), eq("pago_10")))
                .thenReturn(new ImageService.Upload(
                        "http://cloud/pago.jpg",
                        "public-id",
                        "jpg"
                ));

        when(pagoRepository.save(any(Pago.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(pagoMapper.toDTO(any(Pago.class)))
                .thenAnswer(invocation -> {
                    Pago pago = invocation.getArgument(0);
                    return new PagoResponseDTO(
                            1L,
                            pago.getMetodo().name(),
                            pago.getMonto(),
                            pago.getFechaPago(),
                            pago.getComprobanteUrl()
                    );
                });

        PagoResponseDTO resultado = service.registrarPago(dto, comprobante);

        assertEquals("http://cloud/pago.jpg", resultado.getComprobanteUrl());

        verify(imageService).uploadImage(eq(comprobante), eq("pagos"), eq("pago_10"));
    }

    @Test
    void registrarPagoTransferenciaDomicilioDeberiaFallar() {
        pedidoDomicilio.setEstado(EstadoPedido.CUENTA_PEDIDA);

        dto.setPedidoId(11L);
        dto.setMetodo("TRANSFERENCIA");

        when(pedidoDAO.obtenerEntidadPorId(11L)).thenReturn(pedidoDomicilio);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPago(
                        dto,
                        new MockMultipartFile("c", "a.jpg", "image/jpeg", "img".getBytes())
                )
        );

        assertEquals("Transferencia no permitida en domicilio", ex.getMessage());
    }

    // ============================
    // CONSULTA
    // ============================

    @Test
    void obtenerPorPedidoDeberiaRetornarDto() {
        Pago pago = TestDataFactory.pago(1L, pedidoMesa, MetodoPago.EFECTIVO, new BigDecimal("25000"));

        PagoResponseDTO dto = new PagoResponseDTO(
                1L,
                "EFECTIVO",
                new BigDecimal("25000"),
                pago.getFechaPago(),
                null
        );

        when(pagoRepository.findByPedidoId(10L)).thenReturn(Optional.of(pago));
        when(pagoMapper.toDTO(pago)).thenReturn(dto);

        PagoResponseDTO resultado = service.obtenerPorPedido(10L);

        assertSame(dto, resultado);
    }
}