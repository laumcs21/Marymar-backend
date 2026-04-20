package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.SoporteDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface SoporteService {
    List<SoporteDTO> obtenerSoportes(Long pedidoId, String fecha);

    List<String> obtenerSoportes(Long pedidoId, LocalDateTime inicio, LocalDateTime fin);
}
