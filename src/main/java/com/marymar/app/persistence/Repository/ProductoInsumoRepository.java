package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.ProductoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductoInsumoRepository extends JpaRepository<ProductoInsumo, Long> {

    List<ProductoInsumo> findByProductoId(Long productoId);

    boolean existsByProducto_IdAndInsumo_Id(Long productoId, Long insumoId);
    boolean existsByInsumoId (Long insumoId);

    @Query("SELECT pi FROM ProductoInsumo pi JOIN FETCH pi.producto WHERE pi.insumo.id = :insumoId")
    List<ProductoInsumo> findByInsumoId(@Param("insumoId") Long insumoId);}