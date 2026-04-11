package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.business.Service.CategoriaService;
import com.marymar.app.business.Service.GoogleIdTokenService;
import com.marymar.app.business.Service.ImageService;
import com.marymar.app.business.Service.ProductoService;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Repository.ProductoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class ProductoServiceIntegrationTest {

    @MockitoBean
    private ImageService imageService;

    @Autowired
    private ProductoService productoService;
    @Autowired
    private CategoriaService categoriaService;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    @Test
    void deberiaCrearProductoCorrectamente() {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Ejecutivo"));
        ProductoCreateDTO dto = new ProductoCreateDTO("Mojarra", new BigDecimal("25000"), categoria.getId(), "Plato ejecutivo");

        ProductoResponseDTO creado = productoService.crear(dto, null);

        assertNotNull(creado.getId());
        assertEquals("Mojarra", creado.getNombre());
        assertTrue(productoRepository.findById(creado.getId()).isPresent());
    }

    @Test
    void deberiaCrearProductoConImagenes() throws Exception {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        ProductoCreateDTO dto = new ProductoCreateDTO("Cazuela", new BigDecimal("35000"), categoria.getId(), "Especial del día");
        MockMultipartFile img1 = new MockMultipartFile("imagenes", "a.jpg", "image/jpeg", "img1".getBytes());
        MockMultipartFile img2 = new MockMultipartFile("imagenes", "b.jpg", "image/jpeg", "img2".getBytes());
        when(imageService.uploadImage(eq(img1), eq("productos"), anyString()))
                .thenReturn(new ImageService.Upload("http://img/1.jpg", "p1", "jpg"));
        when(imageService.uploadImage(eq(img2), eq("productos"), anyString()))
                .thenReturn(new ImageService.Upload("http://img/2.jpg", "p2", "jpg"));

        ProductoResponseDTO creado = productoService.crear(dto, List.of(img1, img2));
        entityManager.flush();
        entityManager.clear();

        Producto entidad = productoRepository.findById(creado.getId()).orElseThrow();
        assertEquals(2, entidad.getImagenes().size());
        assertEquals("http://img/1.jpg", creado.getImagenPrincipal());
    }

    @Test
    void deberiaActualizarProductoCorrectamente() {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Ejecutivo"));
        ProductoResponseDTO creado = productoService.crear(
                new ProductoCreateDTO("Mojarra", new BigDecimal("25000"), categoria.getId(),
                        "Plato ejecutivo"),
                null
        );

        ProductoResponseDTO actualizado = productoService.actualizar(
                creado.getId(),
                new ProductoCreateDTO("Mojarra Premium", new BigDecimal("28000"),
                        categoria.getId(), "Actualizado"),
                null
        );
        entityManager.flush();
        entityManager.clear();

        Producto entidad = productoRepository.findById(creado.getId()).orElseThrow();
        assertEquals("Mojarra Premium", actualizado.getNombre());
        assertEquals("Mojarra Premium", entidad.getNombre());
        assertEquals(new BigDecimal("28000.00"), entidad.getPrecio().setScale(2));
    }

    @Test
    void desactivarDeberiaAlternarEstado() {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Ejecutivo"));
        ProductoResponseDTO creado = productoService.crear(
                new ProductoCreateDTO("Mojarra", new BigDecimal("25000"), categoria.getId(), "Plato ejecutivo"),
                null
        );

        productoService.desactivar(creado.getId());
        entityManager.flush();
        entityManager.clear();
        assertFalse(productoRepository.findById(creado.getId()).orElseThrow().isActivo());

        productoService.desactivar(creado.getId());
        entityManager.flush();
        entityManager.clear();
        assertTrue(productoRepository.findById(creado.getId()).orElseThrow().isActivo());
    }

    @Test
    void obtenerPorCategoriaDeberiaTraerProductosDeLaCategoria() {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Ejecutivo"));
        productoService.crear(new ProductoCreateDTO("Mojarra", new BigDecimal("25000"), categoria.getId(), "Plato ejecutivo"), null);

        List<ProductoResponseDTO> productos = productoService.obtenerPorCategoria(categoria.getId());

        assertEquals(1, productos.size());
        assertEquals(categoria.getId(), productos.get(0).getCategoriaId());
    }

    @Test
    void eliminarDefinitivoDeberiaEliminarCloudinaryYRegistro() throws Exception {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Especiales"));
        MockMultipartFile img1 = new MockMultipartFile("imagenes", "a.jpg", "image/jpeg", "img1".getBytes());
        when(imageService.uploadImage(eq(img1), eq("productos"), anyString()))
                .thenReturn(new ImageService.Upload("https://res.cloudinary.com/demo/image/upload/v123/marymar/productos/a.jpg", "p1", "jpg"));
        ProductoResponseDTO creado = productoService.crear(
                new ProductoCreateDTO("Cazuela", new BigDecimal("35000"), categoria.getId(), "Especial del día"),
                List.of(img1)
        );
        when(imageService.tryExtractPublicId("https://res.cloudinary.com/demo/image/upload/v123/marymar/productos/a.jpg"))
                .thenReturn("marymar/productos/a");

        productoService.eliminarDefinitivo(creado.getId());
        entityManager.flush();
        entityManager.clear();

        verify(imageService).deleteByPublicId("marymar/productos/a");
        assertTrue(productoRepository.findById(creado.getId()).isEmpty());
    }

    @Test
    void noDeberiaPermitirPrecioNegativo() {
        var categoria = categoriaService.crear(new CategoriaCreateDTO("Ejecutivo"));
        ProductoCreateDTO dto = new ProductoCreateDTO("Mojarra", new BigDecimal("-1000"), categoria.getId(), "Plato ejecutivo");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> productoService.crear(dto, null));

        assertEquals("El precio debe ser mayor a 0", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirCategoriaInexistente() {
        ProductoCreateDTO dto = new ProductoCreateDTO("Plato especial", new BigDecimal("30000"), 999L, "Especial");

        assertThrows(RuntimeException.class, () -> productoService.crear(dto, null));
    }
}
