package com.marymar.app.persistence.Repository;

import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.persistence.Entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByNombre(String nombre);
    @Query("""
           SELECT new com.marymar.app.business.DTO.CategoriaResponseDTO(
               c.id,
               c.nombre,
               COUNT(p.id)
           )
           FROM Categoria c
           LEFT JOIN c.productos p
           GROUP BY c.id, c.nombre
           """)
    List<CategoriaResponseDTO> listarConCantidadProductos();

    @Query("""
           SELECT COUNT(p.id)
           FROM Producto p
           WHERE p.categoria.id = :categoriaId
           """)
    Long contarProductosPorCategoria(Long categoriaId);    boolean existsByNombre(String nombre);
}
