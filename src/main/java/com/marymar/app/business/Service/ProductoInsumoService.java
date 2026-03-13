package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.ProductoInsumoCreateDTO;
import com.marymar.app.persistence.Entity.ProductoInsumo;

import java.util.List;

public interface ProductoInsumoService {

    void agregarInsumoAProducto(ProductoInsumoCreateDTO dto);

    List<ProductoInsumo> obtenerInsumosProducto(Long productoId);

    void actualizarCantidad(Long id, Integer cantidad);

    void eliminar(Long id);
}