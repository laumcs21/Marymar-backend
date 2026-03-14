package com.marymar.app.controller;

import com.marymar.app.business.DTO.ProductoInsumoCreateDTO;
import com.marymar.app.business.Service.ProductoInsumoService;
import com.marymar.app.persistence.Entity.ProductoInsumo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/producto-insumo")
public class ProductoInsumoController {

    private final ProductoInsumoService service;

    public ProductoInsumoController(ProductoInsumoService service){
        this.service = service;
    }

    @PostMapping
    public void crear(@RequestBody ProductoInsumoCreateDTO dto){
        service.agregarInsumoAProducto(dto);
    }

    @GetMapping("/producto/{productoId}")
    public List<Map<String,Object>> listar(@PathVariable Long productoId){
        return service.obtenerInsumosProducto(productoId);
    }

    @PutMapping("/{id}")
    public void actualizarCantidad(@PathVariable Long id,
                                   @RequestParam Integer cantidad){
        service.actualizarCantidad(id, cantidad);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id){
        service.eliminar(id);
    }
}
