package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.PagoCreateDTO;
import com.marymar.app.business.DTO.PagoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoService {

    // =========================
    // REGISTRAR PAGO
    // =========================
    PagoResponseDTO registrarPago(PagoCreateDTO dto, MultipartFile comprobante);

    PagoResponseDTO obtenerPorPedido(Long pedidoId);

    List<String> obtenerSoportes(Long pedidoId, LocalDateTime inicio, LocalDateTime fin);
}