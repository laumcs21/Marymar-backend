package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.business.Service.impl.CategoriaServiceImpl;
import com.marymar.app.persistence.DAO.CategoriaDAO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceUnitTest {

    @Mock
    private CategoriaDAO categoriaDAO;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private CategoriaCreateDTO dto;

    @BeforeEach
    void setUp() {
        dto = new CategoriaCreateDTO("Mariscos");
    }

    @Test
    void deberiaCrearCategoriaCorrectamente() {
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "Mariscos", 0L);
        when(categoriaDAO.existePorNombre("Mariscos")).thenReturn(false);
        when(categoriaDAO.crear(dto)).thenReturn(response);

        CategoriaResponseDTO resultado = categoriaService.crear(dto);

        assertEquals("Mariscos", resultado.getNombre());
        verify(categoriaDAO).crear(dto);
    }

    @Test
    void noDeberiaPermitirNombreVacio() {
        dto.setNombre("");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.crear(dto));

        assertEquals("El nombre de la categoría es obligatorio", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirNombreMuyCorto() {
        dto.setNombre("AB");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.crear(dto));

        assertEquals("El nombre debe tener mínimo 3 caracteres", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirCategoriaDuplicada() {
        when(categoriaDAO.existePorNombre("Mariscos")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.crear(dto));

        assertEquals("Ya existe una categoría con ese nombre", ex.getMessage());
    }

    @Test
    void obtenerPorIdYObtenerTodasDeberianDelegar() {
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "Mariscos", 2L);
        when(categoriaDAO.obtenerPorId(1L)).thenReturn(response);
        when(categoriaDAO.listarConCantidadProductos()).thenReturn(List.of(response));

        assertSame(response, categoriaService.obtenerPorId(1L));
        assertEquals(1, categoriaService.obtenerTodas().size());
    }

    @Test
    void actualizarDeberiaPermitirMismoNombreSinConsultarDuplicado() {
        Categoria categoria = new Categoria("Mariscos");
        CategoriaResponseDTO response = new CategoriaResponseDTO(1L, "Mariscos", 0L);
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(categoriaDAO.actualizar(1L, dto)).thenReturn(response);

        CategoriaResponseDTO resultado = categoriaService.actualizar(1L, dto);

        assertEquals("Mariscos", resultado.getNombre());
        verify(categoriaDAO, never()).existePorNombre("Mariscos");
    }

    @Test
    void actualizarDeberiaFallarSiNuevoNombreYaExiste() {
        Categoria categoria = new Categoria("Pescados");
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(categoriaDAO.existePorNombre("Mariscos")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.actualizar(1L, dto));

        assertEquals("Ya existe una categoría con ese nombre", ex.getMessage());
    }

    @Test
    void eliminarDeberiaFallarSiTieneProductosAsociados() {

        when(categoriaDAO.obtenerEntidadPorId(4L)).thenReturn(new Categoria("Mariscos"));

        when(categoriaDAO.contarProductosPorCategoria(4L))
                .thenReturn(1L);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> categoriaService.eliminar(4L));

        assertEquals("No se puede eliminar la categoría porque tiene productos asociados", ex.getMessage());

        verify(categoriaDAO, never()).eliminar(anyLong());
    }

    @Test
    void eliminarDeberiaDelegarSiNoTieneProductos() {
        Categoria categoria = new Categoria("Mariscos");
        when(categoriaDAO.obtenerEntidadPorId(4L)).thenReturn(categoria);

        categoriaService.eliminar(4L);

        verify(categoriaDAO).eliminar(4L);
    }
}
