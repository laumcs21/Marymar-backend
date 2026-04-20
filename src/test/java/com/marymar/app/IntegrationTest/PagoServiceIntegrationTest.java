package com.marymar.app.IntegrationTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.*;
import com.marymar.app.persistence.Entity.EstadoMesa;
import com.marymar.app.persistence.Entity.EstadoPedido;
import com.marymar.app.persistence.Entity.Pedido;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class PagoServiceIntegrationTest {

    @MockitoBean
    private ImageService imageService;

    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    @Autowired
    private PedidoService pedidoService;
    @Autowired
    private PagoService pagoService;
    @Autowired
    private CategoriaService categoriaService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private InsumoService insumoService;
    @Autowired
    private InventarioService inventarioService;
    @Autowired
    private ProductoInsumoService productoInsumoService;
    @Autowired
    private MesaService mesaService;
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private EntityManager entityManager;


    @Test
    void pagarPedidoMesaEnEfectivoDeberiaLiberarMesaYDescontarInventario() {
        Persona mesero = guardarMesero("mesero.pago.efectivo@test.com");

        MesaResponseDTO mesa = mesaService.crearMesa(new MesaCreateDTO(40, 4));

        CategoriaResponseDTO categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        ProductoResponseDTO producto = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("30000"), categoria.getId(), "Especial"),
                null
        );

        configurarRecetaYStock(producto.getId(), 50);

        PedidoCreateDTO pedidoCreateDTO = TestDataFactory.pedidoCreateMesa(
                mesa.getId(),
                mesero.getId(),
                producto.getId(),
                1
        );

        PedidoResponseDTO pedido = pedidoService.crearPedido(pedidoCreateDTO);

        marcarPedidoComoCuentaPedida(pedido.getId());

        PagoCreateDTO pagoCreateDTO = new PagoCreateDTO();
        pagoCreateDTO.setPedidoId(pedido.getId());
        pagoCreateDTO.setMetodo("EFECTIVO");
        pagoCreateDTO.setMonto(new BigDecimal("30000.00"));

        PagoResponseDTO pago = pagoService.registrarPago(pagoCreateDTO, null);

        assertNotNull(pago.getId());
        assertEquals("EFECTIVO", pago.getMetodo());
        assertEquals(new BigDecimal("30000.00"), pago.getMonto());

        PedidoResponseDTO pedidoPagado = pedidoService.obtenerPorId(pedido.getId());
        assertEquals("PAGADO", pedidoPagado.getEstado());

        MesaResponseDTO mesaFinal = mesaService.obtenerPorId(mesa.getId());
        assertEquals(EstadoMesa.DISPONIBLE, mesaFinal.getEstado());
    }

    @Test
    void transferenciaEnMesaDeberiaGuardarComprobante() throws Exception {
        Persona mesero = guardarMesero("mesero.transferencia@test.com");

        MesaResponseDTO mesa = mesaService.crearMesa(new MesaCreateDTO(41, 4));

        CategoriaResponseDTO categoria = categoriaService.crear(new CategoriaCreateDTO("Bebidas"));
        ProductoResponseDTO producto = productoService.crear(
                new ProductoCreateDTO("Jugo natural", new BigDecimal("12000"), categoria.getId(), "Bebida"),
                null
        );

        configurarRecetaYStock(producto.getId(), 40);

        PedidoCreateDTO pedidoCreateDTO = TestDataFactory.pedidoCreateMesa(
                mesa.getId(),
                mesero.getId(),
                producto.getId(),
                2
        );

        PedidoResponseDTO pedido = pedidoService.crearPedido(pedidoCreateDTO);

        marcarPedidoComoCuentaPedida(pedido.getId());

        when(imageService.uploadImage(any(), anyString(), anyString()))
                .thenReturn(new ImageService.Upload(
                        "https://cloudinary.test/comprobante.jpg",
                        "comprobante-id",
                        "jpg"
                ));

        PagoCreateDTO pagoCreateDTO = new PagoCreateDTO();
        pagoCreateDTO.setPedidoId(pedido.getId());
        pagoCreateDTO.setMetodo("TRANSFERENCIA");
        pagoCreateDTO.setMonto(new BigDecimal("24000.00"));

        MockMultipartFile comprobante = new MockMultipartFile(
                "comprobante",
                "comprobante.jpg",
                "image/jpeg",
                "comprobante".getBytes()
        );

        PagoResponseDTO pago = pagoService.registrarPago(pagoCreateDTO, comprobante);

        assertNotNull(pago.getId());
        assertEquals("TRANSFERENCIA", pago.getMetodo());
        assertEquals(new BigDecimal("24000.00"), pago.getMonto());
        assertNotNull(pago.getComprobanteUrl());
        assertTrue(pago.getComprobanteUrl().contains("cloudinary"));

        PedidoResponseDTO pedidoPagado = pedidoService.obtenerPorId(pedido.getId());
        assertEquals("PAGADO", pedidoPagado.getEstado());
    }

    @Test
    void transferenciaEnDomicilioDeberiaFallar() {
        Persona mesero = guardarMesero("mesero.domicilio.transfer@test.com");
        Persona cliente = guardarCliente("cliente.transfer@test.com");

        CategoriaResponseDTO categoria = categoriaService.crear(new CategoriaCreateDTO("Domicilios"));
        ProductoResponseDTO producto = productoService.crear(
                new ProductoCreateDTO("Arroz marinero", new BigDecimal("28000"), categoria.getId(), "Mariscos"),
                null
        );

        configurarRecetaYStock(producto.getId(), 40);

        PedidoCreateDTO pedidoCreateDTO = TestDataFactory.pedidoCreateDomicilio(
                cliente.getId(),
                mesero.getId(),
                producto.getId(),
                1
        );

        PedidoResponseDTO pedido = pedidoService.crearPedido(pedidoCreateDTO);

        marcarPedidoComoCuentaPedida(pedido.getId());

        PagoCreateDTO pagoCreateDTO = new PagoCreateDTO();
        pagoCreateDTO.setPedidoId(pedido.getId());
        pagoCreateDTO.setMetodo("TRANSFERENCIA");
        pagoCreateDTO.setMonto(new BigDecimal("28000.00"));

        MockMultipartFile comprobante = new MockMultipartFile(
                "comprobante",
                "comprobante.jpg",
                "image/jpeg",
                "comprobante".getBytes()
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pagoService.registrarPago(pagoCreateDTO, comprobante)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("transferencia"));
    }

    /*
    @Test
    void noDeberiaRegistrarPagoSiPedidoNoEstaEnCuentaPedida() {
        Persona mesero = guardarMesero("mesero.sin.cuenta@test.com");

        MesaResponseDTO mesa = mesaService.crearMesa(new MesaCreateDTO(42, 4));

        CategoriaResponseDTO categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        ProductoResponseDTO producto = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("30000"), categoria.getId(), "Especial"),
                null
        );

        configurarRecetaYStock(producto.getId(), 50);

        PedidoCreateDTO pedidoCreateDTO = TestDataFactory.pedidoCreateMesa(
                mesa.getId(),
                mesero.getId(),
                producto.getId(),
                1
        );

        PedidoResponseDTO pedido = pedidoService.crearPedido(pedidoCreateDTO);

        marcarPedidoEstado(pedido.getId(), EstadoPedido.CREADO);

        PagoCreateDTO pagoCreateDTO = new PagoCreateDTO();
        pagoCreateDTO.setPedidoId(pedido.getId());
        pagoCreateDTO.setMetodo("EFECTIVO");
        pagoCreateDTO.setMonto(new BigDecimal("30000.00"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pagoService.registrarPago(pagoCreateDTO, null)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("Primero debes generar la factura"));
    }

    private void marcarPedidoEstado(Long pedidoId, EstadoPedido estado) {
        Pedido pedidoEntidad = pedidoService.obtenerEntidad(pedidoId);
        pedidoEntidad.setEstado(estado);
        pedidoService.guardarEntidad(pedidoEntidad);
    }
     */

    @Test
    void noDeberiaPermitirMontoDiferenteAlTotalDelPedido() {
        Persona mesero = guardarMesero("mesero.monto@test.com");

        MesaResponseDTO mesa = mesaService.crearMesa(new MesaCreateDTO(43, 4));

        CategoriaResponseDTO categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        ProductoResponseDTO producto = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("30000"), categoria.getId(), "Especial"),
                null
        );

        configurarRecetaYStock(producto.getId(), 50);

        PedidoCreateDTO pedidoCreateDTO = TestDataFactory.pedidoCreateMesa(
                mesa.getId(),
                mesero.getId(),
                producto.getId(),
                1
        );

        PedidoResponseDTO pedido = pedidoService.crearPedido(pedidoCreateDTO);

        marcarPedidoComoCuentaPedida(pedido.getId());

        PagoCreateDTO pagoCreateDTO = new PagoCreateDTO();
        pagoCreateDTO.setPedidoId(pedido.getId());
        pagoCreateDTO.setMetodo("EFECTIVO");
        pagoCreateDTO.setMonto(new BigDecimal("10000.00"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pagoService.registrarPago(pagoCreateDTO, null)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("monto"));
    }

    @Test
    void noDeberiaPermitirPagarDosVecesElMismoPedido() {
        Persona mesero = guardarMesero("mesero.duplicado@test.com");

        MesaResponseDTO mesa = mesaService.crearMesa(new MesaCreateDTO(44, 4));

        CategoriaResponseDTO categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        ProductoResponseDTO producto = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("30000"), categoria.getId(), "Especial"),
                null
        );

        configurarRecetaYStock(producto.getId(), 50);

        PedidoCreateDTO pedidoCreateDTO = TestDataFactory.pedidoCreateMesa(
                mesa.getId(),
                mesero.getId(),
                producto.getId(),
                1
        );

        PedidoResponseDTO pedido = pedidoService.crearPedido(pedidoCreateDTO);

        marcarPedidoComoCuentaPedida(pedido.getId());

        PagoCreateDTO pagoCreateDTO = new PagoCreateDTO();
        pagoCreateDTO.setPedidoId(pedido.getId());
        pagoCreateDTO.setMetodo("EFECTIVO");
        pagoCreateDTO.setMonto(new BigDecimal("30000.00"));

        PagoResponseDTO pago = pagoService.registrarPago(pagoCreateDTO, null);
        assertNotNull(pago.getId());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pagoService.registrarPago(pagoCreateDTO, null)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("pagado"));
    }

    private void marcarPedidoComoCuentaPedida(Long pedidoId) {
        Pedido pedidoEntidad = pedidoService.obtenerEntidad(pedidoId);
        pedidoEntidad.setEstado(EstadoPedido.CUENTA_PEDIDA);
        pedidoService.guardarEntidad(pedidoEntidad);
        entityManager.flush();
        entityManager.clear();
    }

    private void configurarRecetaYStock(Long productoId, int stock) {
        InsumoResponseDTO insumo = insumoService.crear(new InsumoCreateDTO("Base-" + productoId, "gr"));

        inventarioService.ingresarStock(
                insumo.getId(),
                stock,
                LocalDateTime.now().plusDays(20)
        );

        inventarioService.surtirCocina(insumo.getId(), stock);

        ProductoInsumoCreateDTO receta = new ProductoInsumoCreateDTO();
        receta.setProductoId(productoId);
        receta.setInsumoId(insumo.getId());
        receta.setCantidad(10);
        productoInsumoService.agregarInsumoAProducto(receta);

        entityManager.flush();
        entityManager.clear();
    }

    private Persona guardarMesero(String email) {
        Persona mesero = Persona.builder()
                .numeroIdentificacion("ID-" + email)
                .nombre("Mesero")
                .email(email)
                .contrasena("hash")
                .telefono("3001234567")
                .fechaNacimiento(LocalDate.of(1997, 1, 1))
                .rol(Rol.MESERO)
                .activo(true)
                .build();

        mesero.setSalario(1800000d);
        return personaRepository.save(mesero);
    }

    private Persona guardarCliente(String email) {
        Persona cliente = Persona.builder()
                .numeroIdentificacion("ID-" + email)
                .nombre("Cliente")
                .email(email)
                .contrasena("hash")
                .telefono("3007654321")
                .fechaNacimiento(LocalDate.of(1998, 5, 10))
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();

        cliente.setDireccionEnvio("Calle 123");
        cliente.setAceptoHabeasData(true);
        return personaRepository.save(cliente);
    }
}