package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.ProductoInsumoCreateDTO;
import com.marymar.app.business.Service.ProductoInsumoService;
import com.marymar.app.controller.ProductoInsumoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoInsumoControllerUnitTest {

    @Mock private ProductoInsumoService service;
    private ProductoInsumoController controller;

    @BeforeEach
    void setUp() { controller = new ProductoInsumoController(service); }

    @Test
    void crearDeberiaDelegar() {
        ProductoInsumoCreateDTO dto = new ProductoInsumoCreateDTO();
        dto.setProductoId(1L);
        dto.setInsumoId(2L);
        dto.setCantidad(3);

        controller.crear(dto);

        verify(service).agregarInsumoAProducto(dto);
    }

    @Test
    void listarDeberiaRetornarRelaciones() {
        when(service.obtenerInsumosProducto(1L)).thenReturn(List.of(Map.of("id", 1L, "cantidad", 3, "insumoNombre", "Harina")));

        List<Map<String, Object>> resultado = controller.listar(1L);

        assertEquals(1, resultado.size());
        assertEquals("Harina", resultado.get(0).get("insumoNombre"));
    }

    @Test
    void actualizarCantidadYEliminarDeberianDelegar() {
        controller.actualizarCantidad(5L, 4);
        controller.eliminar(5L);

        verify(service).actualizarCantidad(5L, 4);
        verify(service).eliminar(5L);
    }
}
