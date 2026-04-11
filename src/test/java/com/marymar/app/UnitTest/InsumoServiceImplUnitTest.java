package com.marymar.app.UnitTest;

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
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsumoServiceImplUnitTest {

    @Mock private InsumoDAO insumoDAO;
    @Mock private InsumoRepository insumoRepository;
    @Mock private ProductoInsumoRepository productoInsumoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @InjectMocks private InsumoServiceImpl service;

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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.crear(dto));

        assertEquals("El nombre del insumo es obligatorio", ex.getMessage());
    }

    @Test
    void crearDeberiaFallarSiYaExiste() {
        when(insumoRepository.findByNombre("Harina")).thenReturn(Optional.of(new Insumo("Harina", "kg")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.crear(dto));

        assertEquals("El insumo ya existe", ex.getMessage());
    }

    @Test
    void eliminarDeberiaBorrarInventarioYEntidadSiNoEstaAsociado() {
        Insumo insumo = new Insumo("Harina", "kg");
        Inventario inventario = new Inventario();

        when(insumoRepository.findById(1L)).thenReturn(Optional.of(insumo));
        when(productoInsumoRepository.existsByInsumoId(1L)).thenReturn(false);
        when(inventarioRepository.findByInsumoId(1L)).thenReturn(Optional.of(inventario));

        service.eliminar(1L);

        verify(insumoRepository).findById(1L);
        verify(productoInsumoRepository).existsByInsumoId(1L);
        verify(inventarioRepository).findByInsumoId(1L);
        verify(inventarioRepository).delete(inventario);
        verify(insumoRepository).delete(insumo);
    }

    @Test
    void eliminarDeberiaFallarSiEstaAsociadoAProductos() {
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(new Insumo("Harina", "kg")));
        when(productoInsumoRepository.existsByInsumoId(1L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.eliminar(1L));

        assertEquals("No se puede eliminar el insumo porque está asociado a productos", ex.getMessage());
    }

    @Test
    void actualizarDeberiaFallarSiExisteDuplicadoEnOtroRegistro() {
        Insumo actual = new Insumo("Sal", "kg");
        com.marymar.app.TestSupport.TestDataFactory.setField(actual, "id", 1L);
        Insumo duplicado = new Insumo("Harina", "kg");
        com.marymar.app.TestSupport.TestDataFactory.setField(duplicado, "id", 2L);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(actual));
        when(insumoRepository.findByNombre("Harina")).thenReturn(Optional.of(duplicado));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.actualizar(1L, dto));

        assertEquals("Ya existe un insumo con ese nombre", ex.getMessage());
    }
}
