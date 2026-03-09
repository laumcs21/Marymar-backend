package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.persistence.DAO.CategoriaDAO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Mapper.CategoriaMapper;
import com.marymar.app.persistence.Repository.CategoriaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaDAOUnitTest {

    @Mock
    private CategoriaRepository repository;
    @Mock
    private CategoriaMapper mapper;

    @InjectMocks
    private CategoriaDAO categoriaDAO;

    private Categoria categoria;
    private CategoriaCreateDTO createDTO;
    private CategoriaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria("Mariscos");
        createDTO = new CategoriaCreateDTO("Mariscos");
        responseDTO = new CategoriaResponseDTO(1L, "Mariscos", 0L);
        try {
            java.lang.reflect.Field field = Categoria.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(categoria, 1L);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void crearDeberiaMapearGuardarYRetornarDto() {
        when(mapper.toEntity(createDTO)).thenReturn(categoria);
        when(repository.save(categoria)).thenReturn(categoria);
        when(mapper.toDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO resultado = categoriaDAO.crear(createDTO);

        assertEquals("Mariscos", resultado.getNombre());
        verify(repository).save(categoria);
    }

    @Test
    void obtenerEntidadPorIdDeberiaRetornarCategoria() {
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaDAO.obtenerEntidadPorId(1L);

        assertSame(categoria, resultado);
    }

    @Test
    void obtenerEntidadPorIdDeberiaFallarSiNoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> categoriaDAO.obtenerEntidadPorId(1L));

        assertEquals("Categoria no encontrada con id 1", ex.getMessage());
    }

    @Test
    void obtenerPorIdYObtenerTodasDeberianMapearDtos() {
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
        when(repository.findAll()).thenReturn(List.of(categoria));
        when(mapper.toDTO(categoria)).thenReturn(responseDTO);

        assertSame(responseDTO, categoriaDAO.obtenerPorId(1L));
        assertEquals(1, categoriaDAO.obtenerTodas().size());
    }

    @Test
    void actualizarDeberiaGuardarCambios() {
        when(repository.findById(1L)).thenReturn(Optional.of(categoria));
        when(repository.save(categoria)).thenReturn(categoria);
        when(mapper.toDTO(categoria)).thenReturn(responseDTO);

        CategoriaResponseDTO resultado = categoriaDAO.actualizar(1L, createDTO);

        assertSame(responseDTO, resultado);
        verify(mapper).updateFromDTO(categoria, createDTO);
    }

    @Test
    void eliminarDeberiaEliminarSiExiste() {
        when(repository.existsById(1L)).thenReturn(true);

        categoriaDAO.eliminar(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void eliminarDeberiaFallarSiNoExiste() {
        when(repository.existsById(1L)).thenReturn(false);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> categoriaDAO.eliminar(1L));

        assertEquals("Categoria no encontrada con id 1", ex.getMessage());
    }

    @Test
    void metodosAuxiliaresDeberianDelegarAlRepositorio() {
        when(repository.findByNombre("Mariscos")).thenReturn(Optional.of(categoria));
        when(repository.listarConCantidadProductos()).thenReturn(List.of(responseDTO));

        assertTrue(categoriaDAO.existePorNombre("Mariscos"));
        assertEquals(1, categoriaDAO.listarConCantidadProductos().size());
    }
}
