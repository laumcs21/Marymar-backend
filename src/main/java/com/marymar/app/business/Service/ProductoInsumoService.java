package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.ProductoInsumoCreateDTO;
import com.marymar.app.persistence.Entity.ProductoInsumo;

import java.util.List;
import java.util.Map;

public interface ProductoInsumoService {

    void agregarInsumoAProducto(ProductoInsumoCreateDTO dto);

    List<Map<String,Object>> obtenerInsumosProducto(Long productoId);

    void actualizarCantidad(Long id, Integer cantidad);

    void eliminar(Long id);
}