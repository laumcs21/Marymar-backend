package com.marymar.app.controller;

import com.marymar.app.business.DTO.ProductoInsumoCreateDTO;
import com.marymar.app.business.Service.ProductoInsumoService;
import com.marymar.app.persistence.Entity.ProductoInsumo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/producto-insumos")
public class ProductoInsumosController {

    private final ProductoInsumoService productoInsumoService;

    public ProductoInsumosController(ProductoInsumoService productoInsumoService) {
        this.productoInsumoService = productoInsumoService;
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<Void> agregar(@RequestBody ProductoInsumoCreateDTO dto){
        productoInsumoService.agregarInsumoAProducto(dto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ProductoInsumo>> obtenerPorProducto(@PathVariable Long productoId){
        return ResponseEntity.ok(productoInsumoService.obtenerInsumosProducto(productoId));
    }
}