package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.ProductoInsumoCreateDTO;
import com.marymar.app.business.Service.impl.ProductoInsumoServiceImpl;
import com.marymar.app.persistence.Entity.Insumo;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.ProductoInsumo;
import com.marymar.app.persistence.Repository.InsumoRepository;
import com.marymar.app.persistence.Repository.ProductoInsumoRepository;
import com.marymar.app.persistence.Repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoInsumoServiceImplUnitTest {

    @Mock private ProductoInsumoRepository productoInsumoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InsumoRepository insumoRepository;
    @InjectMocks private ProductoInsumoServiceImpl service;

    private Producto producto;
    private Insumo insumo;

    @BeforeEach
    void setUp() {
        producto = TestDataFactory.producto(1L, "Mojarra", new BigDecimal("25000"), TestDataFactory.categoria(1L, "Especiales"));
        insumo = TestDataFactory.insumo(2L, "Harina", "kg");
    }

    @Test
    void agregarInsumoAProductoDeberiaGuardarRelacionSiEsValida() {
        ProductoInsumoCreateDTO dto = new ProductoInsumoCreateDTO();
        dto.setProductoId(1L);
        dto.setInsumoId(2L);
        dto.setCantidad(3);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(insumoRepository.findById(2L)).thenReturn(Optional.of(insumo));
        when(productoInsumoRepository.existsByProducto_IdAndInsumo_Id(1L, 2L)).thenReturn(false);

        service.agregarInsumoAProducto(dto);

        verify(productoInsumoRepository).save(any(ProductoInsumo.class));
    }

    @Test
    void agregarInsumoAProductoDeberiaFallarSiYaExisteRelacion() {
        ProductoInsumoCreateDTO dto = new ProductoInsumoCreateDTO();
        dto.setProductoId(1L);
        dto.setInsumoId(2L);
        dto.setCantidad(3);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(insumoRepository.findById(2L)).thenReturn(Optional.of(insumo));
        when(productoInsumoRepository.existsByProducto_IdAndInsumo_Id(1L, 2L)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.agregarInsumoAProducto(dto));

        assertEquals("El insumo ya está asociado al producto", ex.getMessage());
    }

    @Test
    void obtenerInsumosProductoDeberiaMapearCamposEsperados() {
        ProductoInsumo relacion = TestDataFactory.productoInsumo(5L, producto, insumo, 4);
        when(productoInsumoRepository.findByProductoId(1L)).thenReturn(List.of(relacion));

        List<Map<String, Object>> resultado = service.obtenerInsumosProducto(1L);

        assertEquals(1, resultado.size());
        assertEquals(5L, resultado.get(0).get("id"));
        assertEquals(4, resultado.get(0).get("cantidad"));
        assertEquals("Harina", resultado.get(0).get("insumoNombre"));
    }

    @Test
    void actualizarCantidadDeberiaFallarSiCantidadNoEsValida() {
        when(productoInsumoRepository.findById(5L)).thenReturn(Optional.of(TestDataFactory.productoInsumo(5L, producto, insumo, 2)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.actualizarCantidad(5L, 0));

        assertEquals("La cantidad debe ser mayor que cero", ex.getMessage());
    }
}
