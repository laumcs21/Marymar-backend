package com.marymar.app.controller;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.business.Service.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    // =========================
    // LISTAR TODAS
    // =========================
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(
                categoriaService.obtenerTodas()
        );
    }

    // =========================
    // CREAR
    // =========================
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crear(
            @RequestBody CategoriaCreateDTO dto
    ) {
        return ResponseEntity.ok(
                categoriaService.crear(dto)
        );
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody CategoriaCreateDTO dto
    ) {
        return ResponseEntity.ok(
                categoriaService.actualizar(id, dto)
        );
    }

    // =========================
    // ELIMINAR
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}