package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.business.DTO.InventarioUpdateDTO;
import com.marymar.app.business.Service.impl.InventarioServiceImpl;
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Mapper.InventarioMapper;
import com.marymar.app.persistence.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplUnitTest {

    @Mock private InventarioRepository inventarioRepository;
    @Mock private InsumoRepository insumoRepository;
    @Mock private InventarioMapper inventarioMapper;
    @Mock private ProductoInsumoRepository productoInsumoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private ConsumoInventarioRepository consumoInventarioRepository;
    @InjectMocks private InventarioServiceImpl service;

    private Insumo insumo;
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        insumo = TestDataFactory.insumo(1L, "Harina", "kg");
        inventario = TestDataFactory.inventario(1L, insumo, 20);
    }

    @Test
    void crearDeberiaPersistirInventarioValido() {
        InventarioCreateDTO dto = new InventarioCreateDTO(1L, 20);
        InventarioResponseDTO response = new InventarioResponseDTO(1L, 1L, "Harina", "kg", 20, LocalDateTime.now(), LocalDateTime.now());
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
    void crearDeberiaFallarSiStockEsNegativo() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.crear(new InventarioCreateDTO(1L, -1)));

        assertEquals("El stock no puede ser negativo", ex.getMessage());
    }

    @Test
    void actualizarDeberiaFallarSiNoEncuentraInventario() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.actualizar(99L, new InventarioUpdateDTO(30)));

        assertEquals("Inventario no encontrado", ex.getMessage());
    }

    @Test
    void descontarInsumosProductoDeberiaFallarSiProductoNoTieneReceta() {
        when(productoInsumoRepository.findByProductoId(5L)).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.descontarInsumosProducto(5L, 2));

        assertEquals("El producto no tiene receta configurada", ex.getMessage());
    }

    @Test
    void descontarInsumosProductoDeberiaRestarStockCorrectamente() {
        Categoria categoria = TestDataFactory.categoria(1L, "Especiales");
        Producto producto = TestDataFactory.producto(10L, "Cazuela", new BigDecimal("30000"), categoria);
        ProductoInsumo receta = TestDataFactory.productoInsumo(1L, producto, insumo, 3);
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(receta));
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));

        service.descontarInsumosProducto(10L, 2);

        assertEquals(14, inventario.getStock());
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void validarStockPedidoDeberiaFallarSiNoHayStockSuficiente() {
        Categoria categoria = TestDataFactory.categoria(1L, "Especiales");
        Producto producto = TestDataFactory.producto(10L, "Cazuela", new BigDecimal("30000"), categoria);
        Pedido pedido = TestDataFactory.pedidoMesa(20L, TestDataFactory.mesa(1L, 4, 4), TestDataFactory.persona(2L, "Mesero", "mesero@test.com", Rol.MESERO), producto, 2);
        ProductoInsumo receta = TestDataFactory.productoInsumo(1L, producto, insumo, 15);
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(receta));
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarStockPedido(pedido));

        assertEquals("Stock insuficiente de Harina", ex.getMessage());
    }

    @Test
    void actualizarDisponibilidadProductosDeberiaDesactivarProductoSiUnInsumoNoTieneStock() {
        Categoria categoria = TestDataFactory.categoria(1L, "Especiales");
        Producto producto = TestDataFactory.producto(10L, "Cazuela", new BigDecimal("30000"), categoria);
        ProductoInsumo relacion = TestDataFactory.productoInsumo(1L, producto, insumo, 1);
        when(productoInsumoRepository.findByInsumoId(1L)).thenReturn(List.of(relacion));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoInsumoRepository.findByProductoId(10L)).thenReturn(List.of(relacion));
        inventario.setStock(0);
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));

        service.actualizarDisponibilidadProductos(1L);

        assertFalse(producto.isActivo());
        verify(productoRepository).save(producto);
    }
}
