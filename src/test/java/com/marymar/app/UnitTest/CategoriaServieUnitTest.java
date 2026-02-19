package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.business.Service.impl.CategoriaServiceImpl;
import com.marymar.app.persistence.DAO.CategoriaDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoriaServiceImplTest {

    @Mock
    private CategoriaDAO categoriaDAO;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deberiaCrearCategoriaCorrectamente() {

        CategoriaCreateDTO dto = new CategoriaCreateDTO("Mariscos");

        CategoriaResponseDTO response =
                new CategoriaResponseDTO(1L, "Mariscos");

        when(categoriaDAO.crear(dto)).thenReturn(response);

        CategoriaResponseDTO resultado =
                categoriaService.crear(dto);

        assertEquals("Mariscos", resultado.getNombre());
        verify(categoriaDAO).crear(dto);
    }

    @Test
    void noDeberiaPermitirNombreVacio() {

        CategoriaCreateDTO dto =
                new CategoriaCreateDTO("");

        assertThrows(IllegalArgumentException.class,
                () -> categoriaService.crear(dto));
    }
}

