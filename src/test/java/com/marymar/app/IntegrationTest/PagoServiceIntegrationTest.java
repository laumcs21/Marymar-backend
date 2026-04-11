package com.marymar.app.IntegrationTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.*;
import com.marymar.app.persistence.Entity.EstadoMesa;
import com.marymar.app.persistence.Entity.EstadoPedido;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.InventarioRepository;
import com.marymar.app.persistence.Repository.MesaRepository;
import com.marymar.app.persistence.Repository.PedidoRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class PagoServiceIntegrationTest {

    @MockitoBean private ImageService imageService;

    @Autowired private PagoService pagoService;
    @Autowired private PedidoService pedidoService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private ProductoService productoService;
    @Autowired private InsumoService insumoService;
    @Autowired private InventarioService inventarioService;
    @Autowired private ProductoInsumoService productoInsumoService;
    @Autowired private MesaService mesaService;
    @Autowired private PersonaRepository personaRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private MesaRepository mesaRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private EntityManager entityManager;
    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    @Test
    void pagarPedidoMesaEnEfectivoDeberiaLiberarMesaYDescontarInventario() {
        Persona mesero = guardarMesero("pago.mesero@test.com");
        var mesa = mesaService.crearMesa(new MesaCreateDTO(31, 4));
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(new ProductoCreateDTO("Mojarra", new BigDecimal("25000"), categoria.getId(), "Frita"), null);
        var insumo = insumoService.crear(new InsumoCreateDTO("Aceite pago", "ml"));
        inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 20));
        ProductoInsumoCreateDTO receta = new ProductoInsumoCreateDTO();
        receta.setProductoId(producto.getId());
        receta.setInsumoId(insumo.getId());
        receta.setCantidad(5);
        productoInsumoService.agregarInsumoAProducto(receta);

        PedidoCreateDTO pedidoCreate = TestDataFactory.pedidoCreateMesa(mesa.getId(), mesero.getId(), producto.getId(), 2);
        var pedido = pedidoService.crearPedido(pedidoCreate);

        PagoCreateDTO pago = new PagoCreateDTO();
        pago.setPedidoId(pedido.getId());
        pago.setMetodo("EFECTIVO");
        pago.setMonto(new BigDecimal("50000"));

        PagoResponseDTO resultado = pagoService.registrarPago(pago, null);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(resultado.getId());
        assertEquals("EFECTIVO", resultado.getMetodo());
        assertEquals(EstadoPedido.PAGADO, pedidoRepository.findById(pedido.getId()).orElseThrow().getEstado());
        assertEquals(EstadoMesa.DISPONIBLE, mesaRepository.findById(mesa.getId()).orElseThrow().getEstado());
        assertEquals(10, inventarioRepository.findByInsumoId(insumo.getId()).orElseThrow().getStock());
        assertNotNull(pagoService.obtenerPorPedido(pedido.getId()).getId());
    }

    @Test
    void transferenciaEnDomicilioDeberiaFallar() {
        Persona cliente = guardarCliente("cliente.pago@test.com");
        Persona mesero = guardarMesero("mesero.domicilio@test.com");
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(new ProductoCreateDTO("Ceviche", new BigDecimal("27000"), categoria.getId(), "Limon"), null);
        var insumo = insumoService.crear(new InsumoCreateDTO("Camarón pago", "gr"));
        inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 20));
        ProductoInsumoCreateDTO receta = new ProductoInsumoCreateDTO();
        receta.setProductoId(producto.getId());
        receta.setInsumoId(insumo.getId());
        receta.setCantidad(5);
        productoInsumoService.agregarInsumoAProducto(receta);

        PedidoCreateDTO pedidoCreate = TestDataFactory.pedidoCreateDomicilio(cliente.getId(), mesero.getId(), producto.getId(), 1);
        var pedido = pedidoService.crearPedido(pedidoCreate);

        PagoCreateDTO pago = new PagoCreateDTO();
        pago.setPedidoId(pedido.getId());
        pago.setMetodo("TRANSFERENCIA");
        pago.setMonto(new BigDecimal("27000"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pagoService.registrarPago(pago, new MockMultipartFile("c", "a.jpg", "image/jpeg", "img".getBytes())));

        assertEquals("Transferencia no permitida en domicilio", ex.getMessage());
    }

    @Test
    void transferenciaEnMesaDeberiaGuardarComprobante() throws Exception {
        Persona mesero = guardarMesero("mesero.transferencia@test.com");
        var mesa = mesaService.crearMesa(new MesaCreateDTO(32, 4));
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(new ProductoCreateDTO("Arroz marinero", new BigDecimal("30000"), categoria.getId(), "Mariscos"), null);
        var insumo = insumoService.crear(new InsumoCreateDTO("Arroz transferencia", "gr"));
        inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 20));
        ProductoInsumoCreateDTO receta = new ProductoInsumoCreateDTO();
        receta.setProductoId(producto.getId());
        receta.setInsumoId(insumo.getId());
        receta.setCantidad(5);
        productoInsumoService.agregarInsumoAProducto(receta);
        when(imageService.uploadImage(org.mockito.ArgumentMatchers.any(), eq("pagos"), anyString()))
                .thenReturn(new ImageService.Upload("http://img/comprobante.jpg", "public-id", "jpg"));

        PedidoCreateDTO pedidoCreate = TestDataFactory.pedidoCreateMesa(mesa.getId(), mesero.getId(), producto.getId(), 1);
        var pedido = pedidoService.crearPedido(pedidoCreate);

        PagoCreateDTO pago = new PagoCreateDTO();
        pago.setPedidoId(pedido.getId());
        pago.setMetodo("TRANSFERENCIA");
        pago.setMonto(new BigDecimal("30000"));

        PagoResponseDTO resultado = pagoService.registrarPago(pago, new MockMultipartFile("c", "a.jpg", "image/jpeg", "img".getBytes()));

        assertEquals("TRANSFERENCIA", resultado.getMetodo());
        assertEquals("http://img/comprobante.jpg", resultado.getComprobanteUrl());
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
                .telefono("3001234567")
                .fechaNacimiento(LocalDate.of(1999, 1, 1))
                .rol(Rol.CLIENTE)
                .activo(true)
                .build();
        cliente.setDireccionEnvio("Calle 45");
        return personaRepository.save(cliente);
    }
}
