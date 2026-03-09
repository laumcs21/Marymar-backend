package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.ProductoImagen;
import com.marymar.app.persistence.Mapper.ProductoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoMapperUnitTest {

    private ProductoMapper mapper;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        mapper = new ProductoMapper();
        categoria = new Categoria("Pescados");
        try {
            java.lang.reflect.Field field = Categoria.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(categoria, 1L);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void toEntityDeberiaRetornarNullSiDtoEsNull() {
        assertNull(mapper.toEntity(null, categoria));
    }

    @Test
    void toEntityDeberiaMapearCamposBasicos() {
        ProductoCreateDTO dto = new ProductoCreateDTO("Mojarra", new BigDecimal("35000"), 1L, "Mojarra frita");

        Producto producto = mapper.toEntity(dto, categoria);

        assertEquals("Mojarra", producto.getNombre());
        assertEquals(new BigDecimal("35000"), producto.getPrecio());
        assertEquals("Mojarra frita", producto.getDescripcion());
        assertSame(categoria, producto.getCategoria());
    }

    @Test
    void toDTODeberiaRetornarNullSiEntidadEsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTODeberiaMapearImagenesYPrincipal() {
        Producto producto = new Producto("Mojarra", new BigDecimal("35000"), categoria, "Mojarra frita");
        producto.setId(10L);
        ProductoImagen img1 = new ProductoImagen();
        img1.setUrl("http://img/1.jpg");
        ProductoImagen img2 = new ProductoImagen();
        img2.setUrl("http://img/2.jpg");
        producto.setImagenes(List.of(img1, img2));

        ProductoResponseDTO dto = mapper.toDTO(producto);

        assertEquals(10L, dto.getId());
        assertEquals(2, dto.getImagenesUrls().size());
        assertEquals("http://img/1.jpg", dto.getImagenPrincipal());
        assertEquals(1L, dto.getCategoriaId());
        assertEquals("Pescados", dto.getCategoriaNombre());
    }

    @Test
    void updateFromDTODeberiaActualizarCamposInformados() {
        Producto producto = new Producto("Viejo", new BigDecimal("1000"), categoria, "desc vieja");
        Categoria nuevaCategoria = new Categoria("Ejecutivos");
        ProductoCreateDTO dto = new ProductoCreateDTO("Nuevo", new BigDecimal("25000"), 2L, "desc nueva");

        mapper.updateFromDTO(producto, dto, nuevaCategoria);

        assertEquals("Nuevo", producto.getNombre());
        assertEquals(new BigDecimal("25000"), producto.getPrecio());
        assertEquals("desc nueva", producto.getDescripcion());
        assertSame(nuevaCategoria, producto.getCategoria());
    }
}
