package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.business.Service.impl.ProductoServiceImpl;
import com.marymar.app.persistence.DAO.CategoriaDAO;
import com.marymar.app.persistence.DAO.ProductoDAO;
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

    @InjectMocks
    private ProductoServiceImpl productoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deberiaCrearProductoCorrectamente() {

        ProductoCreateDTO dto =
                new ProductoCreateDTO("Mojarra", new BigDecimal("35000"), 1L);

        ProductoResponseDTO response =
                new ProductoResponseDTO(1L, "Mojarra",
                        new BigDecimal("35000"), "Pescados", true);

        when(productoDAO.crear(any(), any()))
                .thenReturn(response);

        ProductoResponseDTO resultado =
                productoService.crear(dto);

        assertEquals("Mojarra", resultado.getNombre());
        assertEquals(new BigDecimal("35000"), resultado.getPrecio());

        verify(productoDAO).crear(any(), any());
    }

    @Test
    void noDeberiaPermitirPrecioNegativo() {

        ProductoCreateDTO dto =
                new ProductoCreateDTO("Mojarra",
                        new BigDecimal("-1000"),
                        1L);

        assertThrows(IllegalArgumentException.class,
                () -> productoService.crear(dto));
    }

    @Test
    void noDeberiaPermitirNombreVacio() {

        ProductoCreateDTO dto =
                new ProductoCreateDTO("",
                        new BigDecimal("20000"),
                        1L);

        assertThrows(IllegalArgumentException.class,
                () -> productoService.crear(dto));
    }
}

