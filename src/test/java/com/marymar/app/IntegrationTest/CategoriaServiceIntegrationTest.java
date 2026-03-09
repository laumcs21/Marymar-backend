package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.Service.CategoriaService;
import com.marymar.app.business.Service.ImageService;
import com.marymar.app.business.Service.ProductoService;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Repository.CategoriaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class CategoriaServiceIntegrationTest {

    @MockitoBean
    private ImageService imageService;

    @Autowired
    private CategoriaService categoriaService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void deberiaCrearCategoriaCorrectamente() {
        CategoriaCreateDTO dto = new CategoriaCreateDTO();
        dto.setNombre("Bebidas");

        CategoriaResponseDTO creada = categoriaService.crear(dto);

        assertNotNull(creada.getId());
        assertEquals("Bebidas", creada.getNombre());
        assertTrue(categoriaRepository.findById(creada.getId()).isPresent());
    }

    @Test
    void obtenerTodasDeberiaIncluirCantidadDeProductos() {
        CategoriaResponseDTO categoria = categoriaService.crear(new CategoriaCreateDTO("Ejecutivos"));
        ProductoCreateDTO producto = new ProductoCreateDTO("Mojarra", new BigDecimal("25000"), categoria.getId(), "Plato especial");
        productoService.crear(producto, null);
        entityManager.flush();
        entityManager.clear();

        List<CategoriaResponseDTO> categorias = categoriaService.obtenerTodas();
        CategoriaResponseDTO encontrada = categorias.stream()
                .filter(c -> c.getId().equals(categoria.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(1L, encontrada.getCantidadProductos());
    }

    @Test
    void deberiaActualizarCategoriaCorrectamente() {
        CategoriaResponseDTO creada = categoriaService.crear(new CategoriaCreateDTO("Postres"));

        CategoriaResponseDTO actualizada = categoriaService.actualizar(creada.getId(), new CategoriaCreateDTO("Postres fríos"));
        entityManager.flush();
        entityManager.clear();

        Categoria entidad = categoriaRepository.findById(creada.getId()).orElseThrow();
        assertEquals("Postres fríos", actualizada.getNombre());
        assertEquals("Postres fríos", entidad.getNombre());
    }

    @Test
    void noDeberiaPermitirEliminarCategoriaConProductos() {
        CategoriaResponseDTO categoria = categoriaService.crear(new CategoriaCreateDTO("Mariscos"));
        productoService.crear(new ProductoCreateDTO("Cazuela", new BigDecimal("35000"), categoria.getId(), "Especial"), null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> categoriaService.eliminar(categoria.getId()));

        assertEquals("No se puede eliminar la categoría porque tiene productos asociados", ex.getMessage());
    }

    @Test
    void deberiaEliminarCategoriaSinProductos() {
        CategoriaResponseDTO categoria = categoriaService.crear(new CategoriaCreateDTO("Temporales"));

        categoriaService.eliminar(categoria.getId());
        entityManager.flush();
        entityManager.clear();

        assertTrue(categoriaRepository.findById(categoria.getId()).isEmpty());
    }

    @Test
    void noDeberiaPermitirNombreDuplicado() {
        categoriaService.crear(new CategoriaCreateDTO("Postres"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> categoriaService.crear(new CategoriaCreateDTO("Postres")));

        assertEquals("Ya existe una categoría con ese nombre", ex.getMessage());
    }
}
