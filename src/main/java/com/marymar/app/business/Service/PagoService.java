package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.PagoCreateDTO;
import com.marymar.app.business.DTO.PagoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface PagoService {

    // =========================
    // REGISTRAR PAGO
    // =========================
    PagoResponseDTO registrarPago(PagoCreateDTO dto, MultipartFile comprobante);

    PagoResponseDTO obtenerPorPedido(Long pedidoId);
}