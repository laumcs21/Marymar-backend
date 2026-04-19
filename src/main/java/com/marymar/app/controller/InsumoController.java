package com.marymar.app.controller;

import com.marymar.app.business.DTO.InsumoCreateDTO;
import com.marymar.app.business.DTO.InsumoResponseDTO;
import com.marymar.app.business.Service.InsumoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insumos")
public class InsumoController {

    private final InsumoService insumoService;

    public InsumoController(InsumoService insumoService) {
        this.insumoService = insumoService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('BODEGUERO')")
    @PostMapping
    public ResponseEntity<InsumoResponseDTO> crear(@RequestBody InsumoCreateDTO dto){
        return ResponseEntity.ok(insumoService.crear(dto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('BODEGUERO')")
    @GetMapping("/{id}")
    public ResponseEntity<InsumoResponseDTO> obtener(@PathVariable Long id){
        return ResponseEntity.ok(insumoService.obtenerPorId(id));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('BODEGUERO')")
    @GetMapping
    public ResponseEntity<List<InsumoResponseDTO>> obtenerTodos(){
        return ResponseEntity.ok(insumoService.obtenerTodos());
    }

    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('BODEGUERO')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){

        insumoService.eliminar(id);

        return ResponseEntity.noContent().build();

    }

    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('BODEGUERO')")
    @PutMapping("/{id}")
    public ResponseEntity<InsumoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody InsumoCreateDTO dto){

        return ResponseEntity.ok(insumoService.actualizar(id, dto));
    }
}