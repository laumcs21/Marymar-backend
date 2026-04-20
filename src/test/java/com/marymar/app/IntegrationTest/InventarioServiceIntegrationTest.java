package com.marymar.app.IntegrationTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.*;
import com.marymar.app.business.Service.*;
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Repository.ConsumoInventarioRepository;
import com.marymar.app.persistence.Repository.InventarioRepository;
import com.marymar.app.persistence.Repository.LoteInsumoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class InventarioServiceIntegrationTest {

    @Autowired private InventarioService inventarioService;
    @Autowired private InsumoService insumoService;
    @Autowired private ProductoService productoService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private ProductoInsumoService productoInsumoService;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private LoteInsumoRepository loteInsumoRepository;
    @Autowired private ConsumoInventarioRepository consumoInventarioRepository;
    @Autowired private EntityManager entityManager;

    @MockitoBean private GoogleIdTokenService googleIdTokenService;
    @MockitoBean private ImageService imageService;

    @Test
    void deberiaCrearInventarioCorrectamente() {
        var insumo = insumoService.crear(new InsumoCreateDTO("Harina", "kg"));

        InventarioResponseDTO creado = inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 50));

        assertNotNull(creado.getId());
        assertEquals(50, creado.getStock());
        assertEquals(insumo.getId(), creado.getInsumoId());
        assertEquals(0, inventarioService.obtenerStockBodega(insumo.getId()));
        assertEquals(0, inventarioService.obtenerStockCocina(insumo.getId()));
    }

    @Test
    void noDeberiaPermitirCrearInventarioDuplicado() {
        var insumo = insumoService.crear(new InsumoCreateDTO("Azucar", "kg"));
        inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 10));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 15)));

        assertEquals("El inventario para este insumo ya existe", ex.getMessage());
    }

    @Test
    void actualizarConAumentoDeStockDeberiaCrearLoteEnBodega() {
        var insumo = insumoService.crear(new InsumoCreateDTO("Sal", "kg"));
        var inventario = inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 10));

        InventarioResponseDTO actualizado = inventarioService.actualizar(inventario.getId(), new InventarioUpdateDTO(30));
        entityManager.flush();
        entityManager.clear();

        assertEquals(30, actualizado.getStock());
        assertEquals(20, inventarioService.obtenerStockBodega(insumo.getId()));

        List<LoteDTO> lotes = inventarioService.obtenerLotes(insumo.getId());
        assertEquals(1, lotes.size());
        assertEquals("BODEGA", lotes.get(0).getUbicacion());
        assertEquals(20, lotes.get(0).getCantidadInicial());
        assertEquals(20, lotes.get(0).getCantidadDisponible());
    }

    @Test
    void ingresarStockDeberiaCrearInventarioSiNoExisteYRegistrarLoteEnBodega() {
        var insumo = insumoService.crear(new InsumoCreateDTO("Aceite", "lt"));
        LocalDateTime vencimiento = LocalDateTime.now().plusDays(20);

        inventarioService.ingresarStock(insumo.getId(), 18, vencimiento);
        entityManager.flush();
        entityManager.clear();

        var inventario = inventarioRepository.findByInsumoId(insumo.getId()).orElseThrow();
        assertEquals(18, inventario.getStock());
        assertEquals(18, inventarioService.obtenerStockBodega(insumo.getId()));
        assertEquals(0, inventarioService.obtenerStockCocina(insumo.getId()));

        List<LoteDTO> lotes = inventarioService.obtenerLotes(insumo.getId());
        assertEquals(1, lotes.size());
        assertEquals("BODEGA", lotes.get(0).getUbicacion());
        assertEquals(18, lotes.get(0).getCantidadDisponible());
    }

    @Test
    void noDeberiaPermitirIngresarStockConCantidadInvalida() {
        var insumo = insumoService.crear(new InsumoCreateDTO("Leche", "lt"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> inventarioService.ingresarStock(insumo.getId(), 0, LocalDateTime.now().plusDays(5)));

        assertEquals("Cantidad inválida", ex.getMessage());
    }

    @Test
    void surtirCocinaDeberiaMoverStockDesdeBodegaAplicandoFefo() {
        var insumo = insumoService.crear(new InsumoCreateDTO("Arroz", "gr"));
        LocalDateTime vencimiento1 = LocalDateTime.now().plusDays(5);
        LocalDateTime vencimiento2 = LocalDateTime.now().plusDays(10);

        inventarioService.ingresarStock(insumo.getId(), 5, vencimiento1);
        inventarioService.ingresarStock(insumo.getId(), 10, vencimiento2);
        inventarioService.surtirCocina(insumo.getId(), 8);
        entityManager.flush();
        entityManager.clear();

        assertEquals(8, inventarioService.obtenerStockCocina(insumo.getId()));
        assertEquals(7, inventarioService.obtenerStockBodega(insumo.getId()));

        List<LoteDTO> lotes = inventarioService.obtenerLotes(insumo.getId());
        assertEquals(3, lotes.size());

        long lotesCocina = lotes.stream().filter(l -> "COCINA".equals(l.getUbicacion())).count();
        assertEquals(2, lotesCocina);

        boolean existeLoteBodegaRestante = lotes.stream().anyMatch(l ->
                "BODEGA".equals(l.getUbicacion()) && l.getCantidadDisponible() == 7);
        assertTrue(existeLoteBodegaRestante);
    }


    @Test
    void descontarStockPedidoDeberiaConsumirLotesDeCocinaYRegistrarConsumo() {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Ejecutivo"));
        var productoDTO = productoService.crear(
                new ProductoCreateDTO("Mojarra", new BigDecimal("25000"), categoria.getId(), "Plato ejecutivo"),
                null
        );

        var insumoDTO = insumoService.crear(new InsumoCreateDTO("Aceite", "ml"));
        inventarioService.ingresarStock(insumoDTO.getId(), 20, LocalDateTime.now().plusDays(20));
        inventarioService.surtirCocina(insumoDTO.getId(), 20);

        ProductoInsumoCreateDTO receta = new ProductoInsumoCreateDTO();
        receta.setProductoId(productoDTO.getId());
        receta.setInsumoId(insumoDTO.getId());
        receta.setCantidad(5);
        productoInsumoService.agregarInsumoAProducto(receta);

        Pedido pedido = TestDataFactory.pedidoMesa(
                99L,
                TestDataFactory.mesa(1L, 1, 4),
                TestDataFactory.persona(2L, "Mesero", "mesero@test.com", Rol.MESERO),
                entityManager.find(Producto.class, productoDTO.getId()),
                2
        );

        inventarioService.descontarStockPedido(pedido);
        entityManager.flush();
        entityManager.clear();

        var inventario = inventarioRepository.findByInsumoId(insumoDTO.getId()).orElseThrow();
        assertEquals(10, inventario.getStock());
        assertEquals(10, inventarioService.obtenerStockCocina(insumoDTO.getId()));
        assertEquals(1, consumoInventarioRepository.count());
    }

    @Test
    void obtenerVistaBodegueroDeberiaExponerStocksPorUbicacion() {
        var insumo = insumoService.crear(new InsumoCreateDTO("Azafrán", "gr"));
        inventarioService.ingresarStock(insumo.getId(), 30, LocalDateTime.now().plusDays(15));
        inventarioService.surtirCocina(insumo.getId(), 12);
        entityManager.flush();
        entityManager.clear();

        List<InventarioBodegueroDTO> vista = inventarioService.obtenerVistaBodeguero();

        assertEquals(1, vista.size());
        assertEquals(insumo.getId(), vista.get(0).getInsumoId());
        assertEquals(30, vista.get(0).getStockTotal());
        assertEquals(12, vista.get(0).getStockCocina());
        assertEquals(18, vista.get(0).getStockBodega());
    }

    @Test
    void obtenerLotesDeberiaListarSoloLotesActivos() {
        var insumo = insumoService.crear(new InsumoCreateDTO("Pimienta", "gr"));
        inventarioService.ingresarStock(insumo.getId(), 10, LocalDateTime.now().plusDays(8));
        inventarioService.surtirCocina(insumo.getId(), 4);
        entityManager.flush();
        entityManager.clear();

        List<LoteDTO> lotes = inventarioService.obtenerLotes(insumo.getId());

        assertEquals(2, lotes.size());
        assertTrue(lotes.stream().anyMatch(l -> "BODEGA".equals(l.getUbicacion()) && l.getCantidadDisponible() == 6));
        assertTrue(lotes.stream().anyMatch(l -> "COCINA".equals(l.getUbicacion()) && l.getCantidadDisponible() == 4));
    }
}
