package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Mapper.CategoriaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoriaMapperUnitTest {

    private CategoriaMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CategoriaMapper();
    }

    @Test
    void toEntityDeberiaRetornarNullSiDtoEsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntityDeberiaMapearNombre() {
        Categoria categoria = mapper.toEntity(new CategoriaCreateDTO("Mariscos"));

        assertEquals("Mariscos", categoria.getNombre());
    }

    @Test
    void toDTODebeRetornarNullSiEntidadEsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTODeberiaMapearCamposBasicos() {
        Categoria categoria = new Categoria("Mariscos");
        try {
            java.lang.reflect.Field field = Categoria.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(categoria, 1L);
        } catch (Exception e) {
            fail(e);
        }

        CategoriaResponseDTO dto = mapper.toDTO(categoria);

        assertEquals(1L, dto.getId());
        assertEquals("Mariscos", dto.getNombre());
        assertEquals(0L, dto.getCantidadProductos());
    }

    @Test
    void updateFromDTODeberiaActualizarNombre() {
        Categoria categoria = new Categoria("Anterior");

        mapper.updateFromDTO(categoria, new CategoriaCreateDTO("Nuevo"));

        assertEquals("Nuevo", categoria.getNombre());
    }
}
