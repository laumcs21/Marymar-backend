package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

        List<Auditoria> findAllByOrderByFechaDesc();

        List<Auditoria> findByUsuario(String usuario);

        List<Auditoria> findByAccion(String accion);

        List<Auditoria> findByEntidad(String entidad);

        List<Auditoria> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

        @Query("""
        SELECT a FROM Auditoria a
        WHERE (:usuario IS NULL OR a.usuario = :usuario)
        AND (:accion IS NULL OR a.accion = :accion)
        AND (:entidad IS NULL OR a.entidad = :entidad)
        AND (:fechaInicio IS NULL OR a.fecha >= :fechaInicio)
        AND (:fechaFin IS NULL OR a.fecha <= :fechaFin)
        ORDER BY a.fecha DESC
    """)
        List<Auditoria> filtrar(
                @Param("usuario") String usuario,
                @Param("accion") String accion,
                @Param("entidad") String entidad,
                @Param("fechaInicio") LocalDateTime fechaInicio,
                @Param("fechaFin") LocalDateTime fechaFin
        );
    }
