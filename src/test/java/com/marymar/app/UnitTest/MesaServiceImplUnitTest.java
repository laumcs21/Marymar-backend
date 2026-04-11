package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.DTO.MesaCreateDTO;
import com.marymar.app.business.DTO.MesaResponseDTO;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.business.Service.PedidoService;
import com.marymar.app.business.Service.impl.MesaServiceImpl;
import com.marymar.app.persistence.DAO.MesaDAO;
import com.marymar.app.persistence.DAO.PedidoDAO;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MesaServiceImplUnitTest {

    @Mock private MesaDAO mesaDAO;
    @Mock private PedidoDAO pedidoDAO;
    @Mock private PersonaDAO personaDAO;
    @Mock private PedidoService pedidoService;
    @Mock private AuditoriaService auditoriaService;
    @InjectMocks private MesaServiceImpl service;

    private Mesa mesa;
    private Persona mesero;

    @BeforeEach
    void setUp() {
        mesa = TestDataFactory.mesa(1L, 8, 4);
        mesero = TestDataFactory.persona(2L, "Mesero", "mesero@test.com", Rol.MESERO);
    }

    @Test
    void crearMesaDeberiaIniciarDisponibleYActiva() {
        when(mesaDAO.guardar(any(Mesa.class))).thenAnswer(invocation -> {
            Mesa guardada = invocation.getArgument(0);
            MesaResponseDTO dto = new MesaResponseDTO();
            dto.setNumero(guardada.getNumero());
            dto.setCapacidad(guardada.getCapacidad());
            dto.setEstado(guardada.getEstado());
            dto.setActiva(guardada.isActiva());
            return dto;
        });

        MesaResponseDTO resultado = service.crearMesa(new MesaCreateDTO(8, 4));

        assertEquals(8, resultado.getNumero());
        assertEquals(EstadoMesa.DISPONIBLE, resultado.getEstado());
        assertTrue(resultado.isActiva());
    }

    @Test
    void abrirMesaDeberiaAsignarMeseroOCuparMesaYCrearPedido() {
        MesaResponseDTO response = new MesaResponseDTO();
        response.setEstado(EstadoMesa.OCUPADA);
        when(mesaDAO.obtenerEntidad(1L)).thenReturn(mesa);
        when(personaDAO.obtenerEntidadPorId(2L)).thenReturn(mesero);
        when(mesaDAO.obtenerPorId(1L)).thenReturn(response);

        MesaResponseDTO resultado = service.abrirMesa(1L, 2L);

        assertEquals(EstadoMesa.OCUPADA, mesa.getEstado());
        assertEquals(mesero, mesa.getMeseroAsignado());
        assertSame(response, resultado);
        verify(pedidoService).obtenerOCrearPedidoPorMesa(1L, 2L);
    }

    @Test
    void abrirMesaDeberiaFallarSiYaEstaOcupada() {
        mesa.setEstado(EstadoMesa.OCUPADA);
        when(mesaDAO.obtenerEntidad(1L)).thenReturn(mesa);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.abrirMesa(1L, 2L));

        assertEquals("La mesa ya está ocupada", ex.getMessage());
    }

    @Test
    void cerrarMesaDeberiaMarcarPedidoPagadoYLiberarMesa() {
        Pedido pedido = TestDataFactory.pedidoMesa(5L, mesa, mesero, null, 0);
        when(mesaDAO.obtenerEntidad(1L)).thenReturn(mesa);
        when(pedidoDAO.obtenerPedidoActivoPorMesa(1L)).thenReturn(pedido);
        when(mesaDAO.actualizar(mesa)).thenAnswer(invocation -> {
            Mesa actualizada = invocation.getArgument(0);
            MesaResponseDTO dto = new MesaResponseDTO();
            dto.setEstado(actualizada.getEstado());
            dto.setActiva(actualizada.isActiva());
            return dto;
        });

        MesaResponseDTO resultado = service.cerrarMesa(1L);

        assertEquals(EstadoPedido.PAGADO, pedido.getEstado());
        assertEquals(EstadoMesa.DISPONIBLE, mesa.getEstado());
        assertNull(mesa.getMeseroAsignado());
        assertEquals(EstadoMesa.DISPONIBLE, resultado.getEstado());
        verify(pedidoDAO).actualizar(pedido);
    }

    @Test
    void cancelarMesaDeberiaEliminarPedidoActivoYLiberarMesa() {
        Pedido pedido = TestDataFactory.pedidoMesa(5L, mesa, mesero, null, 0);
        mesa.setMeseroAsignado(mesero);
        when(mesaDAO.obtenerEntidad(1L)).thenReturn(mesa);
        when(pedidoDAO.obtenerPedidoActivoPorMesa(1L)).thenReturn(pedido);
        when(mesaDAO.actualizar(mesa)).thenAnswer(invocation -> new MesaResponseDTO());

        service.cancelarMesa(1L);

        verify(pedidoDAO).eliminar(5L);
        assertEquals(EstadoMesa.DISPONIBLE, mesa.getEstado());
        assertNull(mesa.getMeseroAsignado());
    }

    @Test
    void eliminarMesaDeberiaFallarSiMesaEstaOcupada() {
        mesa.setEstado(EstadoMesa.OCUPADA);
        when(mesaDAO.obtenerEntidad(1L)).thenReturn(mesa);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.eliminarMesa(1L));

        assertEquals("No puedes eliminar una mesa ocupada", ex.getMessage());
    }

    @Test
    void cambiarEstadoActivoDeberiaFallarSiSeIntentaDesactivarMesaOcupada() {
        mesa.setEstado(EstadoMesa.OCUPADA);
        when(mesaDAO.obtenerEntidad(1L)).thenReturn(mesa);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.cambiarEstadoActivo(1L, false));

        assertEquals("No puedes desactivar una mesa ocupada", ex.getMessage());
    }
}
