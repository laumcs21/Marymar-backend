package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.PedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.business.Service.InventarioService;
import com.marymar.app.business.Service.impl.PedidoServiceImpl;
import com.marymar.app.persistence.DAO.MesaDAO;
import com.marymar.app.persistence.DAO.PedidoDAO;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.DetallePedido;
import com.marymar.app.persistence.Entity.EstadoPedido;
import com.marymar.app.persistence.Entity.Mesa;
import com.marymar.app.persistence.Entity.Pedido;
import com.marymar.app.persistence.Entity.Persona;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.Rol;
import com.marymar.app.persistence.Repository.DetallePedidoRepository;
import com.marymar.app.persistence.Repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplUnitTest {

    @Mock private PedidoDAO pedidoDAO;
    @Mock private PersonaDAO personaDAO;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioService inventarioService;
    @Mock private MesaDAO mesaDAO;
    @Mock private DetallePedidoRepository detallePedidoRepository;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks private PedidoServiceImpl service;

    private Mesa mesa;
    private Persona mesero;
    private Persona cliente;
    private Producto producto;

    @BeforeEach
    void setUp() {
        Categoria categoria = TestDataFactory.categoria(1L, "Especiales");
        mesa = TestDataFactory.mesa(3L, 8, 4);
        mesero = TestDataFactory.persona(2L, "Mesero", "mesero@test.com", Rol.MESERO);
        cliente = TestDataFactory.persona(4L, "Cliente", "cliente@test.com", Rol.CLIENTE);
        producto = TestDataFactory.producto(5L, "Mojarra", new BigDecimal("25000"), categoria);
    }

    // ============================
    // CREAR PEDIDO
    // ============================

    @Test
    void crearPedidoDeberiaFallarSiSolicitudEsNula() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(null)
        );

        assertEquals("La solicitud del pedido es obligatoria", ex.getMessage());
    }

    @Test
    void crearPedidoDeberiaFallarSiTipoEsObligatorio() {
        PedidoCreateDTO dto = new PedidoCreateDTO();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("El tipo de pedido es obligatorio", ex.getMessage());
    }

    @Test
    void crearPedidoDeberiaFallarSiTipoEsInvalido() {
        PedidoCreateDTO dto = new PedidoCreateDTO();
        TestDataFactory.setField(dto, "tipo", "OTRO");
        dto.setDetalles(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("El pedido debe tener al menos un producto", ex.getMessage());
    }

    @Test
    void crearPedidoDeberiaExigirAlMenosUnProducto() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 1);
        dto.setDetalles(new ArrayList<>());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("El pedido debe tener al menos un producto", ex.getMessage());
    }

    @Test
    void crearPedidoMesaDeberiaConstruirPedidoConDetallesYTotal() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 2);

        when(personaDAO.obtenerEntidadPorId(2L)).thenReturn(mesero);
        when(mesaDAO.obtenerEntidad(3L)).thenReturn(mesa);
        when(pedidoDAO.obtenerPedidoActivoPorMesa(3L)).thenReturn(null);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));

        when(pedidoDAO.guardar(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedido = invocation.getArgument(0);
            return new PedidoResponseDTO(
                    1L,
                    pedido.getFecha(),
                    pedido.getEstado().name(),
                    pedido.getTipo().name(),
                    null,
                    2L,
                    "Mesero",
                    3L,
                    8,
                    pedido.getTotal(),
                    List.of(),
                    null
            );
        });

        PedidoResponseDTO response = service.crearPedido(dto);

        assertEquals("MESA", response.getTipo());
        assertEquals(new BigDecimal("50000"), response.getTotal());

        verify(inventarioService).validarStockPedido(any(Pedido.class));
        verify(auditoriaService).registrar(
                eq("CREAR_PEDIDO"),
                eq("PEDIDO"),
                any(),
                contains("50000"),
                isNull()
        );
    }

    @Test
    void crearPedidoMesaDeberiaFallarSiMesaEsObligatoria() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 1);
        dto.setMesaId(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("La mesa es obligatoria para pedidos en mesa", ex.getMessage());
    }

    @Test
    void crearPedidoMesaDeberiaFallarSiMesaYaTienePedidoActivo() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 1);

        when(pedidoDAO.obtenerPedidoActivoPorMesa(3L)).thenReturn(TestDataFactory.pedidoMesa(99L, mesa, mesero, producto, 1));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("La mesa ya tiene un pedido activo. Usa el flujo de edición de mesa", ex.getMessage());
    }

    @Test
    void crearPedidoMesaDeberiaFallarSiMesaEstaInactiva() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 1);
        mesa.setActiva(false);

        when(pedidoDAO.obtenerPedidoActivoPorMesa(3L)).thenReturn(null);
        when(mesaDAO.obtenerEntidad(3L)).thenReturn(mesa);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("La mesa seleccionada está inactiva", ex.getMessage());
    }

    @Test
    void crearPedidoDomicilioDeberiaExigirCliente() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateDomicilio(null, 2L, 5L, 1);
        TestDataFactory.setField(dto, "clienteId", null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("El cliente es obligatorio para domicilio", ex.getMessage());
    }

    @Test
    void crearPedidoDeberiaFallarSiCantidadDeDetalleEsInvalida() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 1);
        dto.getDetalles().get(0).setCantidad(0);

        when(personaDAO.obtenerEntidadPorId(2L)).thenReturn(mesero);
        when(mesaDAO.obtenerEntidad(3L)).thenReturn(mesa);
        when(pedidoDAO.obtenerPedidoActivoPorMesa(3L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("La cantidad de cada producto debe ser mayor a 0", ex.getMessage());
    }

    @Test
    void crearPedidoDeberiaFallarSiProductoNoExiste() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 1);

        when(personaDAO.obtenerEntidadPorId(2L)).thenReturn(mesero);
        when(mesaDAO.obtenerEntidad(3L)).thenReturn(mesa);
        when(pedidoDAO.obtenerPedidoActivoPorMesa(3L)).thenReturn(null);
        when(productoRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("Producto no encontrado", ex.getMessage());
    }

    @Test
    void crearPedidoDeberiaFallarSiProductoNoEstaActivo() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 1);
        producto.setActivo(false);

        when(personaDAO.obtenerEntidadPorId(2L)).thenReturn(mesero);
        when(mesaDAO.obtenerEntidad(3L)).thenReturn(mesa);
        when(pedidoDAO.obtenerPedidoActivoPorMesa(3L)).thenReturn(null);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertTrue(ex.getMessage().contains("no está disponible"));
    }

    // ============================
    // OBTENER O CREAR PEDIDO POR MESA
    // ============================

    @Test
    void obtenerOCrearPedidoPorMesaDeberiaRetornarExistenteSiYaHayActivo() {
        Pedido existente = TestDataFactory.pedidoMesa(7L, mesa, mesero, producto, 1);

        PedidoResponseDTO response = new PedidoResponseDTO(
                7L,
                existente.getFecha(),
                existente.getEstado().name(),
                existente.getTipo().name(),
                null,
                2L,
                "Mesero",
                3L,
                8,
                existente.getTotal(),
                List.of(),
                null
        );

        when(pedidoDAO.obtenerPedidoActivoPorMesa(3L)).thenReturn(existente);
        when(pedidoDAO.actualizar(existente)).thenReturn(response);

        PedidoResponseDTO resultado = service.obtenerOCrearPedidoPorMesa(3L, 2L);

        assertSame(response, resultado);
        verify(pedidoDAO, never()).guardar(any());
    }

    @Test
    void obtenerOCrearPedidoPorMesaDeberiaFallarSiMesaEstaInactiva() {
        mesa.setActiva(false);

        when(pedidoDAO.obtenerPedidoActivoPorMesa(3L)).thenReturn(null);
        when(personaDAO.obtenerEntidadPorId(2L)).thenReturn(mesero);
        when(mesaDAO.obtenerEntidad(3L)).thenReturn(mesa);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.obtenerOCrearPedidoPorMesa(3L, 2L)
        );

        assertEquals("La mesa está inactiva", ex.getMessage());
    }

    @Test
    void obtenerPedidoPorMesaDeberiaFallarSiNoExisteActivo() {
        when(pedidoDAO.obtenerPedidoActivoPorMesa(3L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.obtenerPedidoPorMesa(3L)
        );

        assertEquals("No hay pedido activo para esta mesa", ex.getMessage());
    }

    // ============================
    // AGREGAR PRODUCTO
    // ============================

    @Test
    void agregarProductoDeberiaFallarSiCantidadEsInvalida() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.agregarProducto(1L, 5L, 0)
        );

        assertEquals("La cantidad debe ser mayor a 0", ex.getMessage());
    }

    @Test
    void agregarProductoDeberiaFallarSiProductoNoExiste() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);
        when(productoRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.agregarProducto(1L, 5L, 1)
        );

        assertEquals("Producto no encontrado", ex.getMessage());
    }

    @Test
    void agregarProductoDeberiaFallarSiProductoNoEstaDisponible() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);
        producto.setActivo(false);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.agregarProducto(1L, 5L, 1)
        );

        assertEquals("El producto seleccionado no está disponible", ex.getMessage());
    }

    @Test
    void agregarProductoDeberiaActualizarCantidadYValidarStock() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));

        when(pedidoDAO.actualizar(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido actualizado = invocation.getArgument(0);
            return new PedidoResponseDTO(
                    1L,
                    actualizado.getFecha(),
                    actualizado.getEstado().name(),
                    actualizado.getTipo().name(),
                    null,
                    2L,
                    "Mesero",
                    3L,
                    8,
                    actualizado.getTotal(),
                    List.of(),
                    null
            );
        });

        PedidoResponseDTO resultado = service.agregarProducto(1L, 5L, 2);

        verify(inventarioService).validarStockProductoPedido(5L, 3);
        assertEquals(new BigDecimal("75000"), resultado.getTotal());

        verify(auditoriaService).registrar(
                eq("AGREGAR_PRODUCTO"),
                eq("PEDIDO"),
                eq(1L),
                contains("Cantidad agregada: 2"),
                isNull()
        );
    }

    @Test
    void agregarProductoDeberiaCrearNuevoDetalleSiNoExistia() {
        Pedido pedido = new Pedido(mesa, mesero);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));
        when(pedidoDAO.actualizar(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido actualizado = invocation.getArgument(0);
            return new PedidoResponseDTO(
                    1L,
                    actualizado.getFecha(),
                    actualizado.getEstado().name(),
                    actualizado.getTipo().name(),
                    null,
                    2L,
                    "Mesero",
                    3L,
                    8,
                    actualizado.getTotal(),
                    List.of(),
                    null
            );
        });

        PedidoResponseDTO resultado = service.agregarProducto(1L, 5L, 2);

        verify(inventarioService).validarStockProductoPedido(5L, 2);
        assertEquals(new BigDecimal("50000"), resultado.getTotal());
    }

    @Test
    void agregarProductoDeberiaFallarSiPedidoEstaPagado() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);
        pedido.setEstado(EstadoPedido.PAGADO);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.agregarProducto(1L, 5L, 1)
        );

        assertEquals("No se puede modificar un pedido finalizado", ex.getMessage());
        verify(inventarioService, never()).validarStockProductoPedido(anyLong(), anyInt());
    }

    @Test
    void agregarProductoDeberiaFallarSiPedidoEstaCancelado() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);
        pedido.setEstado(EstadoPedido.CANCELADO);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.agregarProducto(1L, 5L, 1)
        );

        assertEquals("No se puede modificar un pedido finalizado", ex.getMessage());
    }

    // ============================
    // DISMINUIR PRODUCTO
    // ============================

    @Test
    void disminuirProductoConCantidadDeberiaEliminarDetalleSiLlegaACero() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);
        DetallePedido detalle = pedido.getDetalles().get(0);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);
        when(pedidoDAO.actualizar(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido actualizado = invocation.getArgument(0);
            return new PedidoResponseDTO(
                    1L,
                    actualizado.getFecha(),
                    actualizado.getEstado().name(),
                    actualizado.getTipo().name(),
                    null,
                    2L,
                    "Mesero",
                    3L,
                    8,
                    actualizado.getTotal(),
                    List.of(),
                    null
            );
        });

        PedidoResponseDTO resultado = service.disminuirProducto(1L, 5L, 1);

        assertEquals(0, pedido.getDetalles().size());
        assertEquals(BigDecimal.ZERO, resultado.getTotal());
        verify(detallePedidoRepository).delete(detalle);
    }

    @Test
    void disminuirProductoActualDeberiaReducirCantidadEnUno() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 3);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);
        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));
        when(pedidoDAO.actualizar(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido actualizado = invocation.getArgument(0);
            return new PedidoResponseDTO(
                    1L,
                    actualizado.getFecha(),
                    actualizado.getEstado().name(),
                    actualizado.getTipo().name(),
                    null,
                    2L,
                    "Mesero",
                    3L,
                    8,
                    actualizado.getTotal(),
                    List.of(),
                    null
            );
        });

        PedidoResponseDTO resultado = service.disminuirProducto(1L, 5L);

        assertEquals(new BigDecimal("50000"), resultado.getTotal());
        verify(auditoriaService).registrar(
                eq("DISMINUIR_PRODUCTO"),
                eq("PEDIDO"),
                eq(1L),
                contains("Producto disminuido"),
                isNull()
        );
    }

    @Test
    void disminuirProductoActualDeberiaFallarSiProductoNoExiste() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 3);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);
        when(productoRepository.findById(5L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.disminuirProducto(1L, 5L)
        );

        assertEquals("Producto no encontrado", ex.getMessage());
    }

    @Test
    void disminuirProductoActualDeberiaFallarSiPedidoEstaFinalizado() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);
        pedido.setEstado(EstadoPedido.PAGADO);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.disminuirProducto(1L, 5L)
        );

        assertEquals("No se puede modificar un pedido finalizado", ex.getMessage());
    }

    // ============================
    // ELIMINAR DETALLE
    // ============================

    @Test
    void eliminarDetalleDeberiaEliminarCorrectamente() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);
        DetallePedido detalle = pedido.getDetalles().get(0);
        detalle.setId(10L);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);
        when(pedidoDAO.actualizar(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido actualizado = invocation.getArgument(0);
            return new PedidoResponseDTO(
                    1L,
                    actualizado.getFecha(),
                    actualizado.getEstado().name(),
                    actualizado.getTipo().name(),
                    null,
                    2L,
                    "Mesero",
                    3L,
                    8,
                    actualizado.getTotal(),
                    List.of(),
                    null
            );
        });

        PedidoResponseDTO resultado = service.eliminarDetalle(1L, 10L);

        assertEquals(0, pedido.getDetalles().size());
        assertEquals(BigDecimal.ZERO, resultado.getTotal());
    }

    @Test
    void eliminarDetalleDeberiaFallarSiPedidoEstaPagado() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);
        pedido.setEstado(EstadoPedido.PAGADO);

        DetallePedido detalle = pedido.getDetalles().get(0);
        detalle.setId(10L);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.eliminarDetalle(1L, 10L)
        );

        assertEquals("No se puede modificar un pedido finalizado", ex.getMessage());
    }
}