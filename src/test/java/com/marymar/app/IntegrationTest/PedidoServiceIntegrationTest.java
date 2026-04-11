package com.marymar.app.IntegrationTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.*;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class PedidoServiceIntegrationTest {

    @MockitoBean
    private ImageService imageService;

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
    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    @Test
    void deberiaCrearPedidoMesaCorrectamente() {
        Persona mesero = guardarMesero("mesero.mesa@test.com");
        var mesa = mesaService.crearMesa(new MesaCreateDTO(21, 4));
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("30000"), categoria.getId(), "Especial"),
                null
        );

        configurarRecetaYStock(producto.getId(), 50);

        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(mesa.getId(), mesero.getId(), producto.getId(), 1);

        PedidoResponseDTO creado = pedidoService.crearPedido(dto);

        assertNotNull(creado.getId());
        assertEquals("MESA", creado.getTipo());
        assertEquals(mesa.getId(), creado.getMesaId());
        assertEquals(new BigDecimal("30000.00"), creado.getTotal());
        assertEquals(1, creado.getDetalles().size());
        assertEquals(producto.getId(), creado.getDetalles().get(0).getProductoId());
        assertEquals(1, creado.getDetalles().get(0).getCantidad());
    }

    @Test
    void deberiaCrearPedidoDomicilioCorrectamente() {
        Persona mesero = guardarMesero("mesero.domicilio@test.com");
        Persona cliente = guardarCliente("cliente.domicilio@test.com");

        var categoria = categoriaService.crear(new CategoriaCreateDTO("Domicilios"));
        var producto = productoService.crear(
                new ProductoCreateDTO("Arroz marinero", new BigDecimal("28000"), categoria.getId(), "Mariscos"),
                null
        );

        configurarRecetaYStock(producto.getId(), 50);

        PedidoCreateDTO dto = TestDataFactory.pedidoCreateDomicilio(cliente.getId(), mesero.getId(), producto.getId(), 2);

        PedidoResponseDTO creado = pedidoService.crearPedido(dto);

        assertNotNull(creado.getId());
        assertEquals("DOMICILIO", creado.getTipo());
        assertEquals(cliente.getNombre(), creado.getClienteNombre());
        assertNull(creado.getMesaId());
        assertEquals(new BigDecimal("56000.00"), creado.getTotal());
        assertEquals(1, creado.getDetalles().size());
        assertEquals(2, creado.getDetalles().get(0).getCantidad());
    }

    @Test
    void noDeberiaCrearPedidoDomicilioSinCliente() {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(
                new ProductoCreateDTO("Arroz marinero", new BigDecimal("28000"), categoria.getId(), "Mariscos"),
                null
        );

        PedidoCreateDTO dto = new PedidoCreateDTO();
        dto.setDetalles(List.of(new DetallePedidoCreateDTO(producto.getId(), 1)));
        TestDataFactory.setField(dto, "tipo", "DOMICILIO");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pedidoService.crearPedido(dto)
        );

        assertEquals("El cliente es obligatorio para domicilio", ex.getMessage());
    }

    @Test
    void noDeberiaCrearPedidoMesaSinMesa() {
        Persona mesero = guardarMesero("sin.mesa@test.com");
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(
                new ProductoCreateDTO("Sopa", new BigDecimal("12000"), categoria.getId(), "Caliente"),
                null
        );

        PedidoCreateDTO dto = new PedidoCreateDTO();
        dto.setMeseroId(mesero.getId());
        dto.setDetalles(List.of(new DetallePedidoCreateDTO(producto.getId(), 1)));
        TestDataFactory.setField(dto, "tipo", "MESA");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pedidoService.crearPedido(dto)
        );

        assertEquals("La mesa es obligatoria para pedidos en mesa", ex.getMessage());
    }

    @Test
    void noDeberiaCrearPedidoSinDetalles() {
        Persona mesero = guardarMesero("sin.detalles@test.com");
        var mesa = mesaService.crearMesa(new MesaCreateDTO(22, 4));

        PedidoCreateDTO dto = new PedidoCreateDTO();
        dto.setMesaId(mesa.getId());
        dto.setMeseroId(mesero.getId());
        dto.setDetalles(List.of());
        TestDataFactory.setField(dto, "tipo", "MESA");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pedidoService.crearPedido(dto)
        );

        assertEquals("El pedido debe tener al menos un producto", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirAgregarProductoSiNoHayStockSuficiente() {
        Persona mesero = guardarMesero("stock.agregar@test.com");
        var mesa = mesaService.crearMesa(new MesaCreateDTO(23, 4));
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(
                new ProductoCreateDTO("Cazuela especial", new BigDecimal("35000"), categoria.getId(), "Especial"),
                null
        );

        // La receta consume 10 unidades por producto, pero solo hay 15 en stock.
        // Crear 1 producto sí debería pasar; intentar agregar 1 más debería fallar.
        configurarRecetaYStock(producto.getId(), 15);

        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(
                mesa.getId(), mesero.getId(), producto.getId(), 1
        );

        PedidoResponseDTO pedido = pedidoService.crearPedido(dto);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pedidoService.agregarProducto(pedido.getId(), producto.getId(), 1)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("stock"));
    }

    @Test
    void deberiaAgregarProductoYRecalcularTotal() {
        Persona mesero = guardarMesero("agregar.producto@test.com");
        var mesa = mesaService.crearMesa(new MesaCreateDTO(24, 4));
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("30000"), categoria.getId(), "Especial"),
                null
        );

        configurarRecetaYStock(producto.getId(), 80);

        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(mesa.getId(), mesero.getId(), producto.getId(), 1);
        PedidoResponseDTO creado = pedidoService.crearPedido(dto);

        PedidoResponseDTO actualizado = pedidoService.agregarProducto(creado.getId(), producto.getId(), 2);

        assertEquals(1, actualizado.getDetalles().size());
        assertEquals(3, actualizado.getDetalles().get(0).getCantidad());
        assertEquals(new BigDecimal("90000.00"), actualizado.getTotal());
    }

    @Test
    void deberiaDisminuirProductoYRecalcularTotal() {
        Persona mesero = guardarMesero("disminuir.producto@test.com");
        var mesa = mesaService.crearMesa(new MesaCreateDTO(25, 4));
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("30000"), categoria.getId(), "Especial"),
                null
        );

        configurarRecetaYStock(producto.getId(), 80);

        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(mesa.getId(), mesero.getId(), producto.getId(), 3);
        PedidoResponseDTO creado = pedidoService.crearPedido(dto);

        PedidoResponseDTO actualizado = pedidoService.disminuirProducto(creado.getId(), producto.getId(), 1);

        assertEquals(1, actualizado.getDetalles().size());
        assertEquals(2, actualizado.getDetalles().get(0).getCantidad());
        assertEquals(new BigDecimal("60000.00"), actualizado.getTotal());
    }

    @Test
    void disminuirProductoHastaCeroDeberiaEliminarDetalle() {
        Persona mesero = guardarMesero("eliminar.detalle@test.com");
        var mesa = mesaService.crearMesa(new MesaCreateDTO(26, 4));
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(
                new ProductoCreateDTO("Sopa", new BigDecimal("12000"), categoria.getId(), "Caliente"),
                null
        );

        configurarRecetaYStock(producto.getId(), 50);

        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(mesa.getId(), mesero.getId(), producto.getId(), 1);
        PedidoResponseDTO creado = pedidoService.crearPedido(dto);

        PedidoResponseDTO actualizado = pedidoService.disminuirProducto(creado.getId(), producto.getId(), 1);

        assertTrue(actualizado.getDetalles().isEmpty());
        assertEquals(new BigDecimal("0"), actualizado.getTotal());
    }

    @Test
    void noDeberiaPermitirModificarPedidoPagado() {
        Persona mesero = guardarMesero("pedido.pagado@test.com");
        var mesa = mesaService.crearMesa(new MesaCreateDTO(27, 4));
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("30000"), categoria.getId(), "Especial"),
                null
        );

        configurarRecetaYStock(producto.getId(), 80);

        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(mesa.getId(), mesero.getId(), producto.getId(), 1);
        PedidoResponseDTO creado = pedidoService.crearPedido(dto);

        PagoCreateDTO pagoCreateDTO = new PagoCreateDTO();
        pagoCreateDTO.setPedidoId(creado.getId());
        pagoCreateDTO.setMetodo("EFECTIVO");
        pagoCreateDTO.setMonto(new BigDecimal("30000.00"));

        pagoService.registrarPago(pagoCreateDTO, null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> pedidoService.agregarProducto(creado.getId(), producto.getId(), 1)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("pagado"));
    }

    private void configurarRecetaYStock(Long productoId, int stock) {
        var insumo = insumoService.crear(new InsumoCreateDTO("Base-" + productoId, "gr"));
        inventarioService.crear(new InventarioCreateDTO(insumo.getId(), stock));

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