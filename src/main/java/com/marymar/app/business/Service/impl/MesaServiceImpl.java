package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.MesaCreateDTO;
import com.marymar.app.business.DTO.MesaResponseDTO;
import com.marymar.app.business.Service.MesaService;
import com.marymar.app.business.Service.PedidoService;
import com.marymar.app.persistence.DAO.MesaDAO;
import com.marymar.app.persistence.DAO.PedidoDAO;
import com.marymar.app.persistence.DAO.PersonaDAO;
import com.marymar.app.persistence.Entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MesaServiceImpl implements MesaService {

    private final MesaDAO mesaDAO;
    private final PedidoDAO pedidoDAO;
    private final PersonaDAO personaDAO;
    private final PedidoService pedidoService;


    public MesaServiceImpl(MesaDAO mesaDAO,
                           PedidoDAO pedidoDAO,
                           PersonaDAO personaDAO, PedidoService pedidoService) {
        this.mesaDAO = mesaDAO;
        this.pedidoDAO = pedidoDAO;
        this.personaDAO = personaDAO;
        this.pedidoService = pedidoService;
    }

    // =========================
    // CREAR MESA
    // =========================
    @Override
    public MesaResponseDTO crearMesa(MesaCreateDTO dto) {

        Mesa mesa = new Mesa();

        mesa.setNumero(dto.getNumero());
        mesa.setEstado(EstadoMesa.DISPONIBLE);
        mesa.setCapacidad(dto.getCapacidad());
        mesa.setActiva(true);

        return mesaDAO.guardar(mesa);
    }

    // =========================
    // EDITAR MESA
    // =========================
    @Override
    public MesaResponseDTO editarMesa(Long id, MesaCreateDTO dto) {

        Mesa mesa = mesaDAO.obtenerEntidad(id);

        mesa.setNumero(dto.getNumero());

        return mesaDAO.actualizar(mesa);
    }

    // =========================
    // LISTAR
    // =========================
    @Override
    public List<MesaResponseDTO> listar() {
        return mesaDAO.listar();
    }

    // =========================
    // ABRIR MESA
    // =========================
    @Override
    public MesaResponseDTO abrirMesa(Long mesaId, Long meseroId) {

        Mesa mesa = mesaDAO.obtenerEntidad(mesaId);

        if (mesa.getEstado() != EstadoMesa.DISPONIBLE) {
            throw new IllegalArgumentException("La mesa ya está ocupada");
        }

        Persona mesero = personaDAO.obtenerEntidadPorId(meseroId);

        mesa.setMeseroAsignado(mesero);
        mesa.setEstado(EstadoMesa.OCUPADA);

        mesaDAO.actualizar(mesa);

        pedidoService.obtenerOCrearPedidoPorMesa(mesaId, meseroId);
        return mesaDAO.obtenerPorId(mesaId);
    }

    // =========================
    // CERRAR MESA (PAGO)
    // =========================
    @Override
    public MesaResponseDTO cerrarMesa(Long mesaId) {

        Mesa mesa = mesaDAO.obtenerEntidad(mesaId);

        Pedido pedido = pedidoDAO.obtenerPedidoActivoPorMesa(mesaId);

        if (pedido == null) {
            throw new IllegalArgumentException("No hay pedido activo en la mesa");
        }

        pedido.setEstado(EstadoPedido.PAGADO);
        pedidoDAO.actualizar(pedido);

        mesa.setEstado(EstadoMesa.DISPONIBLE);
        mesa.setMeseroAsignado(null);

        return mesaDAO.actualizar(mesa);
    }

    // =========================
    // CANCELAR MESA (CLIENTE SE VA)
    // =========================
    @Override
    @Transactional
    public MesaResponseDTO cancelarMesa(Long mesaId) {

        Mesa mesa = mesaDAO.obtenerEntidad(mesaId);

        Pedido pedido = pedidoDAO.obtenerPedidoActivoPorMesa(mesaId);

        if (pedido != null) {
            pedidoDAO.eliminar(pedido.getId());
        }

        mesa.setEstado(EstadoMesa.DISPONIBLE);
        mesa.setMeseroAsignado(null);

        return mesaDAO.actualizar(mesa);
    }

    // =========================
    // ELIMINAR MESA (CONFIG)
    // =========================
    @Override
    public void eliminarMesa(Long mesaId) {

        Mesa mesa = mesaDAO.obtenerEntidad(mesaId);

        if (mesa.getEstado() != EstadoMesa.DISPONIBLE) {
            throw new IllegalArgumentException("No puedes eliminar una mesa ocupada");
        }

        Pedido pedido = pedidoDAO.obtenerPedidoActivoPorMesa(mesaId);

        if (pedido != null) {
            throw new IllegalArgumentException("No puedes eliminar una mesa con pedido activo");
        }

        mesaDAO.eliminar(mesaId);
    }

    @Override
    public MesaResponseDTO cambiarEstadoActivo(Long id, boolean activa) {

        Mesa mesa = mesaDAO.obtenerEntidad(id);

        if (!activa && mesa.getEstado() == EstadoMesa.OCUPADA) {
            throw new IllegalArgumentException("No puedes desactivar una mesa ocupada");
        }

        mesa.setActiva(activa);

        return mesaDAO.actualizar(mesa);
    }
}