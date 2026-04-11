package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.MesaCreateDTO;
import com.marymar.app.business.DTO.MesaResponseDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;
import com.marymar.app.business.Service.GoogleIdTokenService;
import com.marymar.app.business.Service.MesaService;
import com.marymar.app.business.Service.PedidoService;
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Repository.MesaRepository;
import com.marymar.app.persistence.Repository.PedidoRepository;
import com.marymar.app.persistence.Repository.PersonaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class MesaServiceIntegrationTest {

    @Autowired private MesaService mesaService;
    @Autowired private PedidoService pedidoService;
    @Autowired private PersonaRepository personaRepository;
    @Autowired private MesaRepository mesaRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private EntityManager entityManager;
    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    @Test
    void abrirMesaDeberiaAsignarMeseroYCrearPedidoActivo() {
        Persona mesero = guardarMesero("mesa.mesero@test.com");
        MesaResponseDTO mesa = mesaService.crearMesa(new MesaCreateDTO(12, 4));

        MesaResponseDTO abierta = mesaService.abrirMesa(mesa.getId(), mesero.getId());
        entityManager.flush();
        entityManager.clear();

        assertEquals(EstadoMesa.OCUPADA, abierta.getEstado());
        assertEquals(mesero.getId(), abierta.getMeseroAsignadoId());

        PedidoResponseDTO pedido = pedidoService.obtenerPedidoPorMesa(mesa.getId());
        assertEquals("MESA", pedido.getTipo());
        assertEquals(mesa.getId(), pedido.getMesaId());
        assertEquals(mesero.getId(), pedido.getMeseroId());
    }

    @Test
    void cerrarMesaDeberiaPonerMesaDisponibleYPedidoPagado() {
        Persona mesero = guardarMesero("mesero.cerrar@test.com");
        MesaResponseDTO mesa = mesaService.crearMesa(new MesaCreateDTO(13, 4));
        mesaService.abrirMesa(mesa.getId(), mesero.getId());

        MesaResponseDTO cerrada = mesaService.cerrarMesa(mesa.getId());
        entityManager.flush();
        entityManager.clear();

        assertEquals(EstadoMesa.DISPONIBLE, cerrada.getEstado());
        assertNull(cerrada.getMeseroAsignadoId());
        Pedido pedido = pedidoRepository.findFirstByMesaIdAndEstadoNotIn(mesa.getId(), List.of(EstadoPedido.CANCELADO))
                .orElseThrow();
        assertEquals(EstadoPedido.PAGADO, pedido.getEstado());
    }

    @Test
    void cancelarMesaDeberiaEliminarPedidoActivoYLiberarMesa() {
        Persona mesero = guardarMesero("mesero.cancelar@test.com");
        MesaResponseDTO mesa = mesaService.crearMesa(new MesaCreateDTO(14, 6));
        mesaService.abrirMesa(mesa.getId(), mesero.getId());
        Long pedidoId = pedidoService.obtenerPedidoPorMesa(mesa.getId()).getId();

        MesaResponseDTO cancelada = mesaService.cancelarMesa(mesa.getId());
        entityManager.flush();
        entityManager.clear();

        assertEquals(EstadoMesa.DISPONIBLE, cancelada.getEstado());
        assertNull(cancelada.getMeseroAsignadoId());
        assertTrue(pedidoRepository.findById(pedidoId).isEmpty());
    }

    private Persona guardarMesero(String email) {
        Persona mesero = Persona.builder()
                .numeroIdentificacion("ID-" + email)
                .nombre("Mesero")
                .email(email)
                .contrasena("hash")
                .telefono("3001234567")
                .fechaNacimiento(LocalDate.of(1997, 1, 1))
                .rol(Rol.MESERO)
                .activo(true)
                .build();
        mesero.setSalario(1800000d);
        return personaRepository.save(mesero);
    }
}
