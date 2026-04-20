package com.marymar.app.controller;

import com.marymar.app.business.DTO.PagoCreateDTO;
import com.marymar.app.business.DTO.PagoResponseDTO;
import com.marymar.app.business.Service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // =========================
    // REGISTRAR PAGO
    // =========================
    @PreAuthorize("hasAnyRole('MESERO','CLIENTE')")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PagoResponseDTO> pagar(
            @RequestParam Long pedidoId,
            @RequestParam String metodo,
            @RequestParam BigDecimal monto,
            @RequestParam(required = false) MultipartFile comprobante
    ) {

        PagoCreateDTO dto = new PagoCreateDTO();
        dto.setPedidoId(pedidoId);
        dto.setMetodo(metodo);
        dto.setMonto(monto);

        return ResponseEntity.ok(
                pagoService.registrarPago(dto, comprobante)
        );
    }

    // =========================
    // OBTENER PAGO POR PEDIDO
    // =========================
    @PreAuthorize("hasAnyRole('ADMIN','MESERO')")
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<PagoResponseDTO> obtenerPorPedido(@PathVariable Long pedidoId) {

        return ResponseEntity.ok(
                pagoService.obtenerPorPedido(pedidoId)
        );
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/soportes")
    public ResponseEntity<List<String>> obtenerSoportes(
            @RequestParam Long pedidoId,
            @RequestParam(required = false) LocalDateTime inicio,
            @RequestParam(required = false) LocalDateTime fin
    ) {

        return ResponseEntity.ok(
                pagoService.obtenerSoportes(pedidoId, inicio, fin)
        );
    }
}
