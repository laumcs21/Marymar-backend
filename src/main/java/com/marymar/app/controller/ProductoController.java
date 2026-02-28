package com.marymar.app.controller;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.business.Service.ProductoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // =========================
    // CREAR
    // =========================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductoResponseDTO crear(
            @RequestPart("data") ProductoCreateDTO dto,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes
    ) {
        return productoService.crear(dto, imagenes);
    }

    // =========================
    // OBTENER TODOS
    // =========================
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    // =========================
    // OBTENER POR ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestPart("data") ProductoCreateDTO dto,
            @RequestPart(value = "imagenes", required = false)
            List<MultipartFile> imagenes
    ) {
        return ResponseEntity.ok(
                productoService.actualizar(id, dto, imagenes)
        );
    }

    // =========================
    // DESACTIVAR
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(
            @PathVariable Long id
    ) {
        productoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<ProductoResponseDTO> obtenerPorCategoria(
            @PathVariable Long categoriaId
    ) {
        return productoService.obtenerPorCategoria(categoriaId);
    }

    @DeleteMapping("/definitivo/{id}")
    public void eliminarDefinitivo(@PathVariable Long id) {
        productoService.eliminarDefinitivo(id);
    }
}