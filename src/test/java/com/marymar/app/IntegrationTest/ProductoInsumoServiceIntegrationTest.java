package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class ProductoInsumoServiceIntegrationTest {

    @MockitoBean private ImageService imageService;

    @Autowired private ProductoInsumoService productoInsumoService;
    @Autowired private ProductoService productoService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private InsumoService insumoService;
    @Autowired private EntityManager entityManager;
    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    @Test
    void deberiaCrearListarYActualizarRelacionProductoInsumo() {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(new ProductoCreateDTO("Cazuela", new BigDecimal("32000"), categoria.getId(), "Especial"), null);
        var insumo = insumoService.crear(new InsumoCreateDTO("Camarón", "gr"));

        ProductoInsumoCreateDTO dto = new ProductoInsumoCreateDTO();
        dto.setProductoId(producto.getId());
        dto.setInsumoId(insumo.getId());
        dto.setCantidad(200);

        productoInsumoService.agregarInsumoAProducto(dto);
        entityManager.flush();
        entityManager.clear();

        List<Map<String, Object>> insumos = productoInsumoService.obtenerInsumosProducto(producto.getId());
        assertEquals(1, insumos.size());
        Long relacionId = ((Number) insumos.get(0).get("id")).longValue();
        assertEquals("Camarón", insumos.get(0).get("insumoNombre"));
        assertEquals(200, ((Number) insumos.get(0).get("cantidad")).intValue());

        productoInsumoService.actualizarCantidad(relacionId, 250);
        entityManager.flush();
        entityManager.clear();

        List<Map<String, Object>> actualizados = productoInsumoService.obtenerInsumosProducto(producto.getId());
        assertEquals(250, ((Number) actualizados.get(0).get("cantidad")).intValue());
    }

    @Test
    void noDeberiaPermitirRelacionDuplicada() {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        var producto = productoService.crear(new ProductoCreateDTO("Arroz marinero", new BigDecimal("28000"), categoria.getId(), "Delicioso"), null);
        var insumo = insumoService.crear(new InsumoCreateDTO("Arroz", "gr"));

        ProductoInsumoCreateDTO dto = new ProductoInsumoCreateDTO();
        dto.setProductoId(producto.getId());
        dto.setInsumoId(insumo.getId());
        dto.setCantidad(180);
        productoInsumoService.agregarInsumoAProducto(dto);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productoInsumoService.agregarInsumoAProducto(dto));

        assertEquals("El insumo ya está asociado al producto", ex.getMessage());
    }
}
