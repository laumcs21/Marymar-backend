package com.marymar.app.persistence.Repository;

import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.persistence.Entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombre(String nombre);
    List<Producto> findByCategoriaId(@Param("categoriaId") Long categoriaId);}


