package com.marymar.app.controller;

import com.marymar.app.business.DTO.MesaCreateDTO;
import com.marymar.app.business.DTO.MesaResponseDTO;
import com.marymar.app.business.Service.MesaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    // =========================
    // ADMIN
    // =========================

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<MesaResponseDTO> crear(@RequestBody MesaCreateDTO dto) {
        return ResponseEntity.ok(mesaService.crearMesa(dto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<MesaResponseDTO> editar(
            @PathVariable Long id,
            @RequestBody MesaCreateDTO dto) {

        return ResponseEntity.ok(mesaService.editarMesa(id, dto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mesaService.eliminarMesa(id);
        return ResponseEntity.noContent().build();
    }

    // =========================
    // TODOS (MESERO / ADMIN)
    // =========================

    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MESERO')")
    @GetMapping
    public ResponseEntity<List<MesaResponseDTO>> listar() {
        return ResponseEntity.ok(mesaService.listar());
    }

    // =========================
    // FLUJO DE MESA
    // =========================

    @PreAuthorize("hasRole('MESERO')")
    @PostMapping("/{id}/abrir")
    public ResponseEntity<MesaResponseDTO> abrirMesa(
            @PathVariable Long id,
            @RequestParam Long meseroId) {

        return ResponseEntity.ok(mesaService.abrirMesa(id, meseroId));
    }

    @PreAuthorize("hasRole('MESERO')")
    @PostMapping("/{id}/cerrar")
    public ResponseEntity<MesaResponseDTO> cerrarMesa(
            @PathVariable Long id) {

        return ResponseEntity.ok(mesaService.cerrarMesa(id));
    }

    @PreAuthorize("hasRole('MESERO')")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<MesaResponseDTO> cancelarMesa(
            @PathVariable Long id) {

        return ResponseEntity.ok(mesaService.cancelarMesa(id));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PatchMapping("/{id}/activa")
    public ResponseEntity<MesaResponseDTO> cambiarActiva(
            @PathVariable Long id,
            @RequestBody Boolean activa) {

        return ResponseEntity.ok(
                mesaService.cambiarEstadoActivo(id, activa)
        );
    }
}