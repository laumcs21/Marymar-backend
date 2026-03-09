package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.persistence.DAO.ProductoDAO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Mapper.ProductoMapper;
import com.marymar.app.persistence.Repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoDAOUnitTest {

    @Mock
    private ProductoRepository repository;
    @Mock
    private ProductoMapper mapper;

    @InjectMocks
    private ProductoDAO productoDAO;

    private Categoria categoria;
    private Producto producto;
    private ProductoCreateDTO createDTO;
    private ProductoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        categoria = new Categoria("Pescados");
        producto = new Producto("Mojarra", new BigDecimal("35000"), categoria, "Mojarra frita");
        producto.setId(1L);
        createDTO = new ProductoCreateDTO("Mojarra", new BigDecimal("35000"), 1L, "Mojarra frita");
        responseDTO = new ProductoResponseDTO(1L, "Mojarra", new BigDecimal("35000"),
                "Mojarra frita", 1L, "Pescados", true);
    }

    @Test
    void crearDeberiaMapearGuardarYRetornarDto() {
        when(mapper.toEntity(createDTO, categoria)).thenReturn(producto);
        when(repository.save(producto)).thenReturn(producto);
        when(mapper.toDTO(producto)).thenReturn(responseDTO);

        ProductoResponseDTO resultado = productoDAO.crear(createDTO, categoria);

        assertEquals("Mojarra", resultado.getNombre());
        verify(repository).save(producto);
    }

    @Test
    void obtenerEntidadPorIdDeberiaRetornarProducto() {
        when(repository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoDAO.obtenerEntidadPorId(1L);

        assertSame(producto, resultado);
    }

    @Test
    void obtenerEntidadPorIdDeberiaFallarSiNoExiste() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> productoDAO.obtenerEntidadPorId(1L));

        assertEquals("Producto no encontrado con id 1", ex.getMessage());
    }

    @Test
    void obtenerPorIdYObtenerTodosDeberianMapearDtos() {
        when(repository.findById(1L)).thenReturn(Optional.of(producto));
        when(repository.findAll()).thenReturn(List.of(producto));
        when(mapper.toDTO(producto)).thenReturn(responseDTO);

        assertSame(responseDTO, productoDAO.obtenerPorId(1L));
        assertEquals(1, productoDAO.obtenerTodos().size());
    }

    @Test
    void actualizarDeberiaModificarYGuardar() {
        when(repository.findById(1L)).thenReturn(Optional.of(producto));
        when(repository.save(producto)).thenReturn(producto);
        when(mapper.toDTO(producto)).thenReturn(responseDTO);

        ProductoResponseDTO resultado = productoDAO.actualizar(1L, createDTO, categoria);

        assertSame(responseDTO, resultado);
        verify(mapper).updateFromDTO(producto, createDTO, categoria);
        verify(repository).save(producto);
    }

    @Test
    void desactivarDeberiaGuardarProductoInactivo() {
        when(repository.findById(1L)).thenReturn(Optional.of(producto));

        productoDAO.desactivar(1L);

        assertFalse(producto.isActivo());
        verify(repository).save(producto);
    }

    @Test
    void obtenerPorCategoriaDeberiaMapearLista() {
        when(repository.findByCategoriaId(1L)).thenReturn(List.of(producto));
        when(mapper.toDTO(producto)).thenReturn(responseDTO);

        List<ProductoResponseDTO> resultado = productoDAO.obtenerPorCategoria(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void guardarEntidadDeberiaDelegarAlRepositorio() {
        when(repository.save(producto)).thenReturn(producto);

        Producto resultado = productoDAO.guardarEntidad(producto);

        assertSame(producto, resultado);
    }

    @Test
    void eliminarDefinitivoDeberiaEliminarSiExiste() {
        when(repository.existsById(1L)).thenReturn(true);

        productoDAO.eliminarDefinitivo(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void eliminarDefinitivoDeberiaFallarSiNoExiste() {
        when(repository.existsById(1L)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productoDAO.eliminarDefinitivo(1L));

        assertEquals("Producto no encontrado", ex.getMessage());
    }
}
