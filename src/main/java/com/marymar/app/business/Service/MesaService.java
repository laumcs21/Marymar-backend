package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.MesaCreateDTO;
import com.marymar.app.business.DTO.MesaResponseDTO;

import java.util.List;

public interface MesaService {

    MesaResponseDTO crearMesa(MesaCreateDTO dto);

    MesaResponseDTO editarMesa(Long id, MesaCreateDTO dto);

    List<MesaResponseDTO> listar();

    MesaResponseDTO abrirMesa(Long mesaId, Long meseroId);

    MesaResponseDTO cerrarMesa(Long mesaId);

    MesaResponseDTO obtenerPorId(Long id);

    MesaResponseDTO cancelarMesa(Long mesaId);

    void eliminarMesa(Long mesaId);

    MesaResponseDTO cambiarEstadoActivo(Long id, boolean activa);
}