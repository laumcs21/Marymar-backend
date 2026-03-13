package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.ProductoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoInsumoRepository extends JpaRepository<ProductoInsumo, Long> {

    List<ProductoInsumo> findByProductoId(Long productoId);

    boolean existsByProductoIdAndInsumoId(Long productoId, Long insumoId);
    boolean existsByInsumoId (Long insumoId);
}