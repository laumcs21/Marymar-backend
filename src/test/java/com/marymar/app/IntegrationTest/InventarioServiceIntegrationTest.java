package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.*;
import com.marymar.app.persistence.Entity.Insumo;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.ProductoInsumo;
import com.marymar.app.persistence.Repository.InventarioRepository;
import com.marymar.app.persistence.Repository.ProductoInsumoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class InventarioServiceIntegrationTest {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProductoInsumoRepository productoInsumoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    // =========================
    // CREAR INVENTARIO
    // =========================
    @Test
    void deberiaCrearInventarioCorrectamente() {

        var insumo = insumoService.crear(new InsumoCreateDTO("Harina", "kg"));

        InventarioCreateDTO dto = new InventarioCreateDTO(insumo.getId(), 50);

        var creado = inventarioService.crear(dto);

        assertNotNull(creado.getId());
        assertEquals(50, creado.getStock());
    }

    // =========================
    // NO STOCK NEGATIVO
    // =========================
    @Test
    void noDeberiaPermitirStockNegativo() {

        var insumo = insumoService.crear(new InsumoCreateDTO("Azucar", "kg"));

        assertThrows(RuntimeException.class,
                () -> inventarioService.crear(new InventarioCreateDTO(insumo.getId(), -1)));
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @Test
    void deberiaActualizarInventario() {

        var insumo = insumoService.crear(new InsumoCreateDTO("Sal", "kg"));
        var inventario = inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 10));

        inventarioService.actualizar(inventario.getId(), new InventarioUpdateDTO(30));

        entityManager.flush();
        entityManager.clear();

        var actualizado = inventarioRepository.findById(inventario.getId()).orElseThrow();

        assertEquals(30, actualizado.getStock());
    }

    // =========================
    // DESCONTAR INVENTARIO
    // =========================
    @Test
    void deberiaDescontarInventarioCorrectamente() {

        // Categoria
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Ejecutivo"));

        // Producto
        var productoDTO = productoService.crear(
                new ProductoCreateDTO("Mojarra", new BigDecimal("25000"),
                        categoria.getId(), "Plato ejecutivo"),
                null
        );

        Producto producto = entityManager.find(Producto.class, productoDTO.getId());

        // Insumo
        var insumoDTO = insumoService.crear(new InsumoCreateDTO("Aceite", "lt"));
        Insumo insumo = entityManager.find(Insumo.class, insumoDTO.getId());

        // Inventario
        inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 20));

        // Receta (RELACIÓN REAL)
        ProductoInsumo receta = new ProductoInsumo();
        receta.setProducto(producto);
        receta.setInsumo(insumo);
        receta.setCantidad(2);

        productoInsumoRepository.save(receta);

        // Acción
        inventarioService.descontarInsumosProducto(producto.getId(), 3);

        entityManager.flush();
        entityManager.clear();

        var inventario = inventarioRepository.findByInsumoId(insumo.getId()).orElseThrow();

        assertEquals(14, inventario.getStock()); // 20 - (2 * 3)
    }

    // =========================
    // STOCK INSUFICIENTE
    // =========================
    @Test
    void noDeberiaPermitirDescuentoSinStock() {

        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especial"));
        var productoDTO = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("30000"),
                        categoria.getId(), "Especial"),
                null
        );

        Producto producto = entityManager.find(Producto.class, productoDTO.getId());

        var insumoDTO = insumoService.crear(new InsumoCreateDTO("Queso", "kg"));
        Insumo insumo = entityManager.find(Insumo.class, insumoDTO.getId());

        inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 2));

        ProductoInsumo receta = new ProductoInsumo();
        receta.setProducto(producto);
        receta.setInsumo(insumo);
        receta.setCantidad(2);

        productoInsumoRepository.save(receta);

        assertThrows(IllegalArgumentException.class,
                () -> inventarioService.descontarInsumosProducto(producto.getId(), 2));
    }

    // =========================
    // ELIMINAR
    // =========================
    @Test
    void deberiaEliminarInventario() {

        var insumo = insumoService.crear(new InsumoCreateDTO("Leche", "lt"));
        var inventario = inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 10));

        inventarioService.eliminar(inventario.getId());

        entityManager.flush();
        entityManager.clear();

        assertTrue(inventarioRepository.findById(inventario.getId()).isEmpty());
    }
}