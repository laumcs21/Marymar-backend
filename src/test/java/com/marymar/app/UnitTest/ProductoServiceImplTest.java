
package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.business.Service.impl.ProductoServiceImpl;
import com.marymar.app.business.Service.ImageService;
import com.marymar.app.persistence.DAO.CategoriaDAO;
import com.marymar.app.persistence.DAO.ProductoDAO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Mapper.ProductoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceImplTest {

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private CategoriaDAO categoriaDAO;

    @Mock
    private ImageService imageService;

    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deberiaCrearProductoCorrectamente() {

        // El DTO ahora incluye también descripción en el constructor.
        ProductoCreateDTO dto =
                new ProductoCreateDTO("Mojarra", new BigDecimal("35000"), 1L, "Mojarra frita");

        ProductoResponseDTO response =
                new ProductoResponseDTO(
                        1L,
                        "Mojarra",
                        new BigDecimal("35000"),
                        "Mojarra frita",
                        1L,
                        "Pescados",
                        true
                );

        // Arrange: dependencias que usa el servicio en la implementación actual
        Categoria categoria = new Categoria();

        Producto entidad = new Producto();
        entidad.setId(1L);

        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoMapper.toEntity(any(ProductoCreateDTO.class), any(Categoria.class))).thenReturn(entidad);
        when(productoDAO.guardarEntidad(any(Producto.class))).thenReturn(entidad);
        when(productoMapper.toDTO(any(Producto.class))).thenReturn(response);

        ProductoResponseDTO resultado =
                productoService.crear(dto, null);

        assertEquals("Mojarra", resultado.getNombre());
        assertEquals(new BigDecimal("35000"), resultado.getPrecio());

        verify(categoriaDAO).obtenerEntidadPorId(1L);
        verify(productoDAO).guardarEntidad(any(Producto.class));
        verify(productoMapper).toDTO(any(Producto.class));
    }

    @Test
    void noDeberiaPermitirPrecioNegativo() {

        ProductoCreateDTO dto =
                new ProductoCreateDTO("Mojarra",
                        new BigDecimal("-1000"),
                        1L,
                        "Mojarra frita");

        assertThrows(IllegalArgumentException.class,
                () -> productoService.crear(dto, null));
    }

    @Test
    void noDeberiaPermitirNombreVacio() {

        ProductoCreateDTO dto =
                new ProductoCreateDTO("",
                        new BigDecimal("20000"),
                        1L,
                        "");

        assertThrows(IllegalArgumentException.class,
                () -> productoService.crear(dto, null));
    }
}