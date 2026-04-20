package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.InsumoCreateDTO;
import com.marymar.app.business.DTO.InsumoResponseDTO;
import com.marymar.app.business.Service.impl.InsumoServiceImpl;
import com.marymar.app.persistence.DAO.InsumoDAO;
import com.marymar.app.persistence.Entity.Inventario;
import com.marymar.app.persistence.Entity.Insumo;
import com.marymar.app.persistence.Repository.InsumoRepository;
import com.marymar.app.persistence.Repository.InventarioRepository;
import com.marymar.app.persistence.Repository.ProductoInsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsumoServiceImplUnitTest {

    @Mock private InsumoDAO insumoDAO;
    @Mock private InsumoRepository insumoRepository;
    @Mock private ProductoInsumoRepository productoInsumoRepository;
    @Mock private InventarioRepository inventarioRepository;

    @InjectMocks
    private InsumoServiceImpl service;

    private InsumoCreateDTO dto;

    @BeforeEach
    void setUp() {
        dto = new InsumoCreateDTO("Harina", "kg");
    }

    @Test
    void crearDeberiaDelegarEnDaoSiEsValido() {
        InsumoResponseDTO response = new InsumoResponseDTO(1L, "Harina", "kg");
        when(insumoRepository.findByNombre("Harina")).thenReturn(Optional.empty());
        when(insumoDAO.crear(dto)).thenReturn(response);

        InsumoResponseDTO resultado = service.crear(dto);

        assertSame(response, resultado);
        verify(insumoDAO).crear(dto);
    }

    @Test
    void crearDeberiaFallarSiNombreEsObligatorio() {
        dto.setNombre(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crear(dto));

        assertEquals("El nombre del insumo es obligatorio", ex.getMessage());
    }

    @Test
    void crearDeberiaFallarSiYaExiste() {
        when(insumoRepository.findByNombre("Harina")).thenReturn(Optional.of(new Insumo("Harina", "kg")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crear(dto));

        assertEquals("El insumo ya existe", ex.getMessage());
    }

    @Test
    void obtenerPorIdDeberiaDelegarEnDao() {
        InsumoResponseDTO response = new InsumoResponseDTO(1L, "Harina", "kg");
        when(insumoDAO.obtenerPorId(1L)).thenReturn(response);

        InsumoResponseDTO resultado = service.obtenerPorId(1L);

        assertSame(response, resultado);
        verify(insumoDAO).obtenerPorId(1L);
    }

    @Test
    void obtenerTodosDeberiaDelegarEnDao() {
        List<InsumoResponseDTO> response = List.of(
                new InsumoResponseDTO(1L, "Harina", "kg"),
                new InsumoResponseDTO(2L, "Azucar", "kg")
        );
        when(insumoDAO.obtenerTodos()).thenReturn(response);

        List<InsumoResponseDTO> resultado = service.obtenerTodos();

        assertEquals(2, resultado.size());
        verify(insumoDAO).obtenerTodos();
    }

    @Test
    void actualizarDeberiaActualizarNombreYUnidadCuandoEsValido() {
        Insumo insumo = new Insumo("Harina", "kg");
        insumo.setId(1L);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(insumoRepository.findByNombre("Harina premium")).thenReturn(Optional.empty());

        InsumoResponseDTO resultado = service.actualizar(1L, new InsumoCreateDTO("Harina premium", "g"));

        assertEquals(1L, resultado.getId());
        assertEquals("Harina premium", resultado.getNombre());
        assertEquals("g", resultado.getUnidad());
        verify(insumoRepository).save(insumo);
    }

    @Test
    void actualizarDeberiaFallarSiNoExisteElInsumo() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.actualizar(1L, dto));

        assertEquals("Insumo no encontrado", ex.getMessage());
    }

    @Test
    void actualizarDeberiaFallarSiNombreEsObligatorio() {
        Insumo insumo = new Insumo("Harina", "kg");
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, new InsumoCreateDTO("   ", "kg")));

        assertEquals("El nombre del insumo es obligatorio", ex.getMessage());
        verify(insumoRepository, never()).save(any());
    }

    @Test
    void actualizarDeberiaFallarSiExisteDuplicadoEnOtroRegistro() {
        Insumo actual = new Insumo("Sal", "kg");
        TestDataFactory.setField(actual, "id", 1L);
        Insumo duplicado = new Insumo("Harina", "kg");
        TestDataFactory.setField(duplicado, "id", 2L);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(insumoRepository.findByNombre("Harina")).thenReturn(Optional.of(duplicado));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.actualizar(1L, dto));

        assertEquals("Ya existe un insumo con ese nombre", ex.getMessage());
    }

    @Test
    void actualizarDeberiaPermitirGuardarSiElDuplicadoEsElMismoRegistro() {
        Insumo insumo = new Insumo("Harina", "kg");
        TestDataFactory.setField(insumo, "id", 1L);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(insumoRepository.findByNombre("Harina")).thenReturn(Optional.of(insumo));

        InsumoResponseDTO resultado = service.actualizar(1L, dto);

        assertEquals("Harina", resultado.getNombre());
        verify(insumoRepository).save(insumo);
    }

    @Test
    void eliminarDeberiaFallarSiElInsumoNoExiste() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.eliminar(1L));

        assertEquals("Insumo no encontrado", ex.getMessage());
    }

    @Test
    void eliminarDeberiaBorrarInventarioYEntidadSiNoEstaAsociado() {
        Insumo insumo = new Insumo("Harina", "kg");
        Inventario inventario = new Inventario();

        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(productoInsumoRepository.existsByInsumoId(1L)).thenReturn(false);
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));

        service.eliminar(1L);

        verify(inventarioRepository).delete(inventario);
        verify(insumoRepository).delete(insumo);
    }

    @Test
    void eliminarDeberiaEliminarEntidadAunqueNoTengaInventario() {
        Insumo insumo = new Insumo("Harina", "kg");
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(productoInsumoRepository.existsByInsumoId(1L)).thenReturn(false);
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.empty());

        service.eliminar(1L);

        verify(inventarioRepository, never()).delete(any());
        verify(insumoRepository).delete(insumo);
    }

    @Test
    void eliminarDeberiaFallarSiEstaAsociadoAProductos() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(new Insumo("Harina", "kg")));
        when(productoInsumoRepository.existsByInsumoId(1L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.eliminar(1L));

        assertEquals("No se puede eliminar el insumo porque está asociado a productos", ex.getMessage());
        verify(insumoRepository, never()).delete(any());
    }
}
