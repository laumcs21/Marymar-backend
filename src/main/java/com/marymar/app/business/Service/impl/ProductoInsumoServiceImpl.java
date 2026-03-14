package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.ProductoInsumoCreateDTO;
import com.marymar.app.business.Service.ProductoInsumoService;
import com.marymar.app.persistence.Entity.Insumo;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.ProductoInsumo;
import com.marymar.app.persistence.Repository.InsumoRepository;
import com.marymar.app.persistence.Repository.ProductoInsumoRepository;
import com.marymar.app.persistence.Repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductoInsumoServiceImpl implements ProductoInsumoService {

    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoRepository productoRepository;
    private final InsumoRepository insumoRepository;

    public ProductoInsumoServiceImpl(
            ProductoInsumoRepository productoInsumoRepository,
            ProductoRepository productoRepository,
            InsumoRepository insumoRepository) {

        this.productoInsumoRepository = productoInsumoRepository;
        this.productoRepository = productoRepository;
        this.insumoRepository = insumoRepository;
    }

    @Override
    public void agregarInsumoAProducto(ProductoInsumoCreateDTO dto) {

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        if(productoInsumoRepository
                .existsByProducto_IdAndInsumo_Id(dto.getProductoId(), dto.getInsumoId())){
            throw new IllegalArgumentException("El insumo ya está asociado al producto");
        }

        if(dto.getCantidad()<=0){
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        ProductoInsumo pi = new ProductoInsumo(producto, insumo, dto.getCantidad());

        productoInsumoRepository.save(pi);
    }

    @Override
    public List<Map<String,Object>> obtenerInsumosProducto(Long productoId){

        List<ProductoInsumo> lista = productoInsumoRepository.findByProductoId(productoId);

        return lista.stream().map(pi -> {

            Map<String,Object> map = new HashMap<>();

            map.put("id", pi.getId());
            map.put("cantidad", pi.getCantidad());
            map.put("insumoNombre", pi.getInsumo().getNombre());

            return map;

        }).toList();
    }
    @Override
    public void actualizarCantidad(Long id, Integer cantidad) {

        ProductoInsumo pi = productoInsumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relación producto-insumo no encontrada"));

        if(cantidad <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        pi.setCantidad(cantidad);

        productoInsumoRepository.save(pi);
    }

    @Override
    public void eliminar(Long id) {

        ProductoInsumo pi = productoInsumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Relación producto-insumo no encontrada"));

        productoInsumoRepository.delete(pi);
    }
}