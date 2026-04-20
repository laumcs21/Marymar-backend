package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.impl.InventarioServiceImpl;
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Mapper.InventarioMapper;
import com.marymar.app.persistence.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplUnitTest {

    @Mock private InventarioRepository inventarioRepository;
    @Mock private InsumoRepository insumoRepository;
    @Mock private LoteInsumoRepository loteInsumoRepository;
    @Mock private InventarioMapper inventarioMapper;
    @Mock private ProductoInsumoRepository productoInsumoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ConsumoInventarioRepository consumoInventarioRepository;

    @InjectMocks
    private InventarioServiceImpl service;

    private Insumo insumo;
    private Inventario inventario;
    private Categoria categoria;
    private Producto producto;
    private ProductoInsumo receta;

    @BeforeEach
    void setUp() {
        insumo = TestDataFactory.insumo(1L, "Harina", "kg");
        inventario = TestDataFactory.inventario(1L, insumo, 20);
        categoria = TestDataFactory.categoria(1L, "Especiales");
        producto = TestDataFactory.producto(10L, "Cazuela", new BigDecimal("30000"), categoria);
        receta = TestDataFactory.productoInsumo(1L, producto, insumo, 3);
    }

    @Test
    void crearDeberiaPersistirInventarioValido() {
        InventarioCreateDTO dto = new InventarioCreateDTO(1L, 20);
        InventarioResponseDTO response = new InventarioResponseDTO(
                1L, 1L, "Harina", "kg", 20, LocalDateTime.now(), LocalDateTime.now()
        );
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.empty());
        when(inventarioMapper.toEntity(dto, insumo)).thenReturn(inventario);
        when(inventarioRepository.save(inventario)).thenReturn(inventario);
        when(inventarioMapper.toDTO(inventario)).thenReturn(response);

        InventarioResponseDTO resultado = service.crear(dto);

        assertSame(response, resultado);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void crearDeberiaFallarSiInsumoNoExiste() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.crear(new InventarioCreateDTO(1L, 10)));

        assertEquals("Insumo no encontrado", ex.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void crearDeberiaFallarSiStockEsNegativo() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.crear(new InventarioCreateDTO(1L, -1)));

        assertEquals("El stock no puede ser negativo", ex.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void crearDeberiaFallarSiYaExisteInventarioParaElInsumo() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.crear(new InventarioCreateDTO(1L, 10)));

        assertEquals("El inventario para este insumo ya existe", ex.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void actualizarDeberiaFallarSiNoEncuentraInventario() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.actualizar(99L, new InventarioUpdateDTO(30)));

        assertEquals("Inventario no encontrado", ex.getMessage());
    }

    @Test
    void actualizarDeberiaFallarSiStockEsNegativo() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.actualizar(1L, new InventarioUpdateDTO(-5)));

        assertEquals("El stock no puede ser negativo", ex.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void actualizarDeberiaRegistrarLoteEnBodegaCuandoAumentaElStock() {
        InventarioUpdateDTO dto = new InventarioUpdateDTO(28);
        InventarioResponseDTO response = new InventarioResponseDTO(
                1L, 1L, "Harina", "kg", 28, LocalDateTime.now(), LocalDateTime.now()
        );
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(inventarioRepository.save(inventario)).thenReturn(inventario);
        when(productoInsumoRepository.findByInsumoId(1L)).thenReturn(List.of());
        when(inventarioMapper.toDTO(inventario)).thenReturn(response);

        InventarioResponseDTO resultado = service.actualizar(1L, dto);

        assertSame(response, resultado);
        assertEquals(28, inventario.getStock());
        ArgumentCaptor<LoteInsumo> captor = ArgumentCaptor.forClass(LoteInsumo.class);
        verify(loteInsumoRepository).save(captor.capture());
        LoteInsumo lote = captor.getValue();
        assertEquals(insumo, lote.getInsumo());
        assertEquals(8, lote.getCantidadInicial());
        assertEquals(8, lote.getCantidadDisponible());
        assertEquals(UbicacionInventario.BODEGA, lote.getUbicacion());
        assertEquals(EstadoLote.ACTIVO, lote.getEstado());
    }

    @Test
    void actualizarNoDeberiaRegistrarLoteSiElStockNoAumenta() {
        InventarioUpdateDTO dto = new InventarioUpdateDTO(12);
        InventarioResponseDTO response = new InventarioResponseDTO(
                1L, 1L, "Harina", "kg", 12, LocalDateTime.now(), LocalDateTime.now()
        );
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(inventario)).thenReturn(inventario);
        when(productoInsumoRepository.findByInsumoId(1L)).thenReturn(List.of());
        when(inventarioMapper.toDTO(inventario)).thenReturn(response);

        service.actualizar(1L, dto);

        assertEquals(12, inventario.getStock());
        verify(loteInsumoRepository, never()).save(any());
    }

    @Test
    void descontarInsumosProductoDeberiaFallarSiProductoNoTieneReceta() {
        when(productoInsumoRepository.findByProductoId(5L)).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.descontarInsumosProducto(5L, 2));

        assertEquals("El producto no tiene receta configurada", ex.getMessage());
    }

    @Test
    void descontarInsumosProductoDeberiaFallarSiNoHayStockEnCocina() {
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(receta));
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.COCINA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote(1L, 4, 4, UbicacionInventario.COCINA, EstadoLote.ACTIVO)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.descontarInsumosProducto(10L, 2));

        assertEquals("Stock insuficiente en cocina de Harina", ex.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void descontarInsumosProductoDeberiaRestarStockTotalSiHayStockEnCocina() {
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(receta));
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.COCINA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote(1L, 10, 10, UbicacionInventario.COCINA, EstadoLote.ACTIVO)));

        service.descontarInsumosProducto(10L, 2);

        assertEquals(14, inventario.getStock());
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void validarStockPedidoDeberiaFallarSiNoHayStockSuficienteEnCocina() {
        Pedido pedido = TestDataFactory.pedidoMesa(
                20L,
                TestDataFactory.mesa(1L, 4, 4),
                TestDataFactory.persona(2L, "Mesero", "mesero@test.com", Rol.MESERO),
                producto,
                2
        );
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(receta));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.COCINA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote(1L, 5, 5, UbicacionInventario.COCINA, EstadoLote.ACTIVO)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarStockPedido(pedido));

        assertEquals("Stock insuficiente en cocina de Harina", ex.getMessage());
    }

    @Test
    void validarStockProductoPedidoDeberiaFallarSiNoHayReceta() {
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.validarStockProductoPedido(10L, 1));

        assertEquals("El producto no tiene receta configurada", ex.getMessage());
    }

    @Test
    void validarStockProductoPedidoDeberiaValidarCantidadAcumulada() {
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(receta));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.COCINA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote(1L, 5, 5, UbicacionInventario.COCINA, EstadoLote.ACTIVO)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarStockProductoPedido(10L, 2));

        assertEquals("Stock insuficiente en cocina de Harina", ex.getMessage());
    }

    @Test
    void descontarStockPedidoDeberiaConsumirLotesGuardarConsumoYActualizarInventario() {
        Pedido pedido = TestDataFactory.pedidoMesa(
                30L,
                TestDataFactory.mesa(1L, 8, 4),
                TestDataFactory.persona(2L, "Mesero", "mesero@test.com", Rol.MESERO),
                producto,
                2
        );

        LoteInsumo lote1 = lote(1L, 5, 5, UbicacionInventario.COCINA, EstadoLote.ACTIVO);
        LoteInsumo lote2 = lote(2L, 4, 4, UbicacionInventario.COCINA, EstadoLote.ACTIVO);

        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(receta));
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.COCINA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote1, lote2));
        when(productoInsumoRepository.findByInsumoId(1L)).thenReturn(List.of(receta));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        service.descontarStockPedido(pedido);

        assertEquals(14, inventario.getStock());

        assertEquals(1, lote1.getCantidadDisponible());
        assertEquals(EstadoLote.ACTIVO, lote1.getEstado());

        assertEquals(4, lote2.getCantidadDisponible());
        assertEquals(EstadoLote.ACTIVO, lote2.getEstado());

        verify(loteInsumoRepository).save(lote1);
        verify(inventarioRepository).save(inventario);
        verify(consumoInventarioRepository).save(any(ConsumoInventario.class));
        verify(productoRepository).save(producto);

        service.descontarStockPedido(pedido);

        System.out.println("Inventario final: " + inventario.getStock());
        System.out.println("Lote1 disponible: " + lote1.getCantidadDisponible());
        System.out.println("Lote1 estado: " + lote1.getEstado());
        System.out.println("Lote2 disponible: " + lote2.getCantidadDisponible());
        System.out.println("Lote2 estado: " + lote2.getEstado());
        System.out.println("Receta cantidad: " + receta.getCantidad());
        System.out.println("Detalle cantidad: " + pedido.getDetalles().get(0).getCantidad());
    }

    @Test
    void actualizarDisponibilidadProductosDeberiaDesactivarProductoSiUnInsumoNoTieneStock() {
        when(productoInsumoRepository.findByInsumoId(1L)).thenReturn(List.of(receta));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(receta));
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.empty());

        service.actualizarDisponibilidadProductos(1L);

        assertFalse(producto.isActivo());
        verify(productoRepository).save(producto);
    }

    @Test
    void actualizarDisponibilidadProductosDeberiaMantenerActivoSiTodosLosInsumosTienenStock() {
        producto.setActivo(false);
        when(productoInsumoRepository.findByInsumoId(1L)).thenReturn(List.of(receta));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(receta));
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));

        service.actualizarDisponibilidadProductos(1L);

        assertTrue(producto.isActivo());
        verify(productoRepository).save(producto);
    }

    @Test
    void registrarEntradaLoteDeberiaPersistirLoteEnBodega() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

        service.registrarEntradaLote(1L, 9, LocalDateTime.of(2026, 5, 20, 10, 0));

        ArgumentCaptor<LoteInsumo> captor = ArgumentCaptor.forClass(LoteInsumo.class);
        verify(loteInsumoRepository).save(captor.capture());
        LoteInsumo lote = captor.getValue();
        assertEquals(insumo, lote.getInsumo());
        assertEquals(9, lote.getCantidadInicial());
        assertEquals(9, lote.getCantidadDisponible());
        assertEquals(UbicacionInventario.BODEGA, lote.getUbicacion());
        assertEquals(EstadoLote.ACTIVO, lote.getEstado());
        assertEquals(LocalDateTime.of(2026, 5, 20, 10, 0), lote.getFechaVencimiento());
    }

    @Test
    void descontarDeLotesDeberiaConsumirVariosLotesConFefo() {
        LoteInsumo lote1 = lote(1L, 4, 4, UbicacionInventario.COCINA, EstadoLote.ACTIVO);
        LoteInsumo lote2 = lote(2L, 6, 6, UbicacionInventario.COCINA, EstadoLote.ACTIVO);
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.COCINA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote1, lote2));

        service.descontarDeLotes(1L, 7);

        assertEquals(0, lote1.getCantidadDisponible());
        assertEquals(EstadoLote.AGOTADO, lote1.getEstado());
        assertEquals(3, lote2.getCantidadDisponible());
        verify(loteInsumoRepository, times(2)).save(any(LoteInsumo.class));
    }

    @Test
    void descontarDeLotesDeberiaFallarSiElStockDeLotesEsInsuficiente() {
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.COCINA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote(1L, 2, 2, UbicacionInventario.COCINA, EstadoLote.ACTIVO)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.descontarDeLotes(1L, 5));

        assertEquals("Stock insuficiente (lotes)", ex.getMessage());
    }

    @Test
    void ingresarStockDeberiaFallarSiLaCantidadEsInvalida() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.ingresarStock(1L, 0, LocalDateTime.now().plusDays(10)));

        assertEquals("Cantidad inválida", ex.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void ingresarStockDeberiaCrearInventarioSiNoExisteYRegistrarLote() {
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.empty());
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.ingresarStock(1L, 12, LocalDateTime.of(2026, 6, 1, 8, 0));

        ArgumentCaptor<Inventario> inventarioCaptor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository, atLeastOnce()).save(inventarioCaptor.capture());
        Inventario ultimoGuardado = inventarioCaptor.getAllValues().get(inventarioCaptor.getAllValues().size() - 1);
        assertEquals(insumo, ultimoGuardado.getInsumo());
        assertEquals(12, ultimoGuardado.getStock());
        verify(loteInsumoRepository).save(any(LoteInsumo.class));
    }

    @Test
    void surtirCocinaDeberiaFallarSiCantidadEsInvalida() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.surtirCocina(1L, -2));

        assertEquals("Cantidad inválida", ex.getMessage());
    }

    @Test
    void surtirCocinaDeberiaMoverStockDesdeBodegaYCrearLoteEnCocina() {
        LoteInsumo loteBodega = lote(1L, 10, 10, UbicacionInventario.BODEGA, EstadoLote.ACTIVO);
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.BODEGA, EstadoLote.ACTIVO
        )).thenReturn(List.of(loteBodega));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndFechaVencimientoAndEstado(
                eq(1L), eq(UbicacionInventario.COCINA), eq(loteBodega.getFechaVencimiento()), eq(EstadoLote.ACTIVO)
        )).thenReturn(Optional.empty());

        service.surtirCocina(1L, 6);

        assertEquals(4, loteBodega.getCantidadDisponible());
        verify(loteInsumoRepository).save(loteBodega);
        ArgumentCaptor<LoteInsumo> captor = ArgumentCaptor.forClass(LoteInsumo.class);
        verify(loteInsumoRepository, atLeast(2)).save(captor.capture());
        boolean cocinaCreada = captor.getAllValues().stream()
                .anyMatch(l -> l.getUbicacion() == UbicacionInventario.COCINA && l.getCantidadDisponible() == 6);
        assertTrue(cocinaCreada);
    }

    @Test
    void surtirCocinaDeberiaAcumularEnLoteCocinaExistenteConMismoVencimiento() {
        LoteInsumo loteBodega = lote(1L, 10, 7, UbicacionInventario.BODEGA, EstadoLote.ACTIVO);
        LoteInsumo loteCocina = lote(2L, 3, 3, UbicacionInventario.COCINA, EstadoLote.ACTIVO);
        loteCocina.setFechaVencimiento(loteBodega.getFechaVencimiento());
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.BODEGA, EstadoLote.ACTIVO
        )).thenReturn(List.of(loteBodega));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndFechaVencimientoAndEstado(
                eq(1L), eq(UbicacionInventario.COCINA), eq(loteBodega.getFechaVencimiento()), eq(EstadoLote.ACTIVO)
        )).thenReturn(Optional.of(loteCocina));

        service.surtirCocina(1L, 5);

        assertEquals(2, loteBodega.getCantidadDisponible());
        assertEquals(8, loteCocina.getCantidadDisponible());
        assertEquals(8, loteCocina.getCantidadInicial());
        verify(loteInsumoRepository).save(loteCocina);
    }

    @Test
    void surtirCocinaDeberiaFallarSiNoHaySuficienteEnBodega() {
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.BODEGA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote(1L, 3, 3, UbicacionInventario.BODEGA, EstadoLote.ACTIVO)));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndFechaVencimientoAndEstado(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.surtirCocina(1L, 5));

        assertEquals("No hay suficiente en bodega para surtir cocina", ex.getMessage());
    }

    @Test
    void obtenerStockCocinaYBodegaDeberiaSumarSoloLotesActivosDeCadaUbicacion() {
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.COCINA, EstadoLote.ACTIVO
        )).thenReturn(List.of(
                lote(1L, 5, 5, UbicacionInventario.COCINA, EstadoLote.ACTIVO),
                lote(2L, 4, 2, UbicacionInventario.COCINA, EstadoLote.ACTIVO)
        ));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.BODEGA, EstadoLote.ACTIVO
        )).thenReturn(List.of(
                lote(3L, 8, 7, UbicacionInventario.BODEGA, EstadoLote.ACTIVO)
        ));

        assertEquals(7, service.obtenerStockCocina(1L));
        assertEquals(7, service.obtenerStockBodega(1L));
    }

    @Test
    void obtenerVistaBodegueroDeberiaConstruirResumenConStocksPorUbicacion() {
        when(inventarioRepository.findAll()).thenReturn(List.of(inventario));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.COCINA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote(1L, 5, 4, UbicacionInventario.COCINA, EstadoLote.ACTIVO)));
        when(loteInsumoRepository.findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                1L, UbicacionInventario.BODEGA, EstadoLote.ACTIVO
        )).thenReturn(List.of(lote(2L, 10, 8, UbicacionInventario.BODEGA, EstadoLote.ACTIVO)));

        List<InventarioBodegueroDTO> resultado = service.obtenerVistaBodeguero();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getInventarioId());
        assertEquals(1L, resultado.get(0).getInsumoId());
        assertEquals("Harina", resultado.get(0).getInsumoNombre());
        assertEquals(20, resultado.get(0).getStockTotal());
        assertEquals(4, resultado.get(0).getStockCocina());
        assertEquals(8, resultado.get(0).getStockBodega());
    }

    @Test
    void obtenerLotesDeberiaMapearLotesActivosADto() {
        LoteInsumo lote = lote(5L, 10, 6, UbicacionInventario.COCINA, EstadoLote.ACTIVO);
        when(loteInsumoRepository.findByInsumoIdAndEstadoOrderByUbicacionAscFechaVencimientoAscFechaIngresoAsc(1L, EstadoLote.ACTIVO))
                .thenReturn(List.of(lote));

        List<LoteDTO> resultado = service.obtenerLotes(1L);

        assertEquals(1, resultado.size());
        assertEquals(5L, resultado.get(0).getId());
        assertEquals(10, resultado.get(0).getCantidadInicial());
        assertEquals(6, resultado.get(0).getCantidadDisponible());
        assertEquals("COCINA", resultado.get(0).getUbicacion());
        assertEquals(lote.getFechaVencimiento(), resultado.get(0).getFechaVencimiento());
    }

    private LoteInsumo lote(Long id, int cantidadInicial, int cantidadDisponible,
                            UbicacionInventario ubicacion, EstadoLote estado) {
        LoteInsumo lote = new LoteInsumo();
        lote.setId(id);
        lote.setInsumo(insumo);
        lote.setCantidadInicial(cantidadInicial);
        lote.setCantidadDisponible(cantidadDisponible);
        lote.setUbicacion(ubicacion);
        lote.setEstado(estado);
        lote.setFechaIngreso(LocalDateTime.of(2026, 4, 1, 8, 0).plusDays(id));
        lote.setFechaVencimiento(LocalDateTime.of(2026, 5, 1, 8, 0).plusDays(id));
        return lote;
    }
}
