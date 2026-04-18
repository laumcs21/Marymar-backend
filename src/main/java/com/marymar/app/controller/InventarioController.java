package com.marymar.app.controller;

import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.business.DTO.InventarioUpdateDTO;
import com.marymar.app.business.Service.InventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<InventarioResponseDTO> crear(@RequestBody InventarioCreateDTO dto){
        return ResponseEntity.ok(inventarioService.crear(dto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<InventarioResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody InventarioUpdateDTO dto){

        return ResponseEntity.ok(inventarioService.actualizar(id, dto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<List<InventarioResponseDTO>> obtenerTodos(){
        return ResponseEntity.ok(inventarioService.obtenerTodos());
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){

        inventarioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    // =========================
    // INGRESAR STOCK (BODEGA)
    // =========================
    @PostMapping("/{insumoId}/ingresar")
    public ResponseEntity<Void> ingresarStock(
            @PathVariable Long insumoId,
            @RequestParam int cantidad,
            @RequestParam String fechaVencimiento
    ) {

        inventarioService.ingresarStock(
                insumoId,
                cantidad,
                LocalDateTime.parse(fechaVencimiento)
        );

        return ResponseEntity.ok().build();
    }

    // =========================
    // SURTIR COCINA
    // =========================
    @PostMapping("/{insumoId}/surtir")
    public ResponseEntity<Void> surtirCocina(
            @PathVariable Long insumoId,
            @RequestParam int cantidad
    ) {

        inventarioService.surtirCocina(insumoId, cantidad);

        return ResponseEntity.ok().build();
    }
}
