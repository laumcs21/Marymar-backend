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
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Repository.DetallePedidoRepository;
import com.marymar.app.persistence.Repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

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
    void crearPedidoDeberiaFallarSiTipoEsObligatorio() {
        PedidoCreateDTO dto = new PedidoCreateDTO();

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("El tipo de pedido es obligatorio", ex.getMessage());
    }

    @Test
    void crearPedidoMesaDeberiaConstruirPedidoConDetallesYTotal() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 2);

        when(personaDAO.obtenerEntidadPorId(2L)).thenReturn(mesero);
        when(mesaDAO.obtenerEntidad(3L)).thenReturn(mesa);
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

        verify(auditoriaService).registrar(
                eq("CREAR_PEDIDO"),
                eq("PEDIDO"),
                any(),
                contains("50000"),
                isNull()
        );
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
    void crearPedidoDeberiaExigirAlMenosUnProducto() {
        PedidoCreateDTO dto = TestDataFactory.pedidoCreateMesa(3L, 2L, 5L, 1);
        dto.setDetalles(new ArrayList<>());

        when(personaDAO.obtenerEntidadPorId(2L)).thenReturn(mesero);
        when(mesaDAO.obtenerEntidad(3L)).thenReturn(mesa);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.crearPedido(dto)
        );

        assertEquals("El pedido debe tener al menos un producto", ex.getMessage());
    }

    // ============================
    // OBTENER O CREAR PEDIDO
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
    void agregarProductoDeberiaFallarSiPedidoNoExiste() {
        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.agregarProducto(1L, 5L, 1)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("pedido"));
    }

    @Test
    void agregarProductoDeberiaFallarSiProductoNoExiste() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);

        when(pedidoDAO.obtenerEntidadPorId(1L)).thenReturn(pedido);
        when(productoRepository.findById(5L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.agregarProducto(1L, 5L, 1)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("producto"));
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
                contains("Cantidad: 2"),
                isNull()
        );
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

        assertEquals("No se puede modificar un pedido pagado", ex.getMessage());

        verify(inventarioService, never()).validarStockProductoPedido(anyLong(), anyInt());
    }

    // ============================
    // DISMINUIR PRODUCTO
    // ============================

    @Test
    void disminuirProductoDeberiaEliminarDetalleSiCantidadLlegaACero() {
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

    // ============================
    // ELIMINAR DETALLE
    // ============================

    @Test
    void eliminarDetalleDeberiaFallarSiPedidoEstaPagado() {
        Pedido pedido = TestDataFactory.pedidoMesa(1L, mesa, mesero, producto, 1);
        pedido.setEstado(EstadoPedido.PAGADO);

        DetallePedido detalle = pedido.getDetalles().get(0);
        detalle.setPedido(pedido);
        detalle.setId(10L);

        when(detallePedidoRepository.findById(10L)).thenReturn(Optional.of(detalle));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.eliminarDetalle(10L)
        );

        assertEquals("No se puede modificar un pedido pagado", ex.getMessage());
    }
}