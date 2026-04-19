package com.marymar.app.persistence.Repository;

import com.marymar.app.business.DTO.LoteDTO;
import com.marymar.app.persistence.Entity.EstadoLote;
import com.marymar.app.persistence.Entity.LoteInsumo;
import com.marymar.app.persistence.Entity.UbicacionInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoteInsumoRepository extends JpaRepository<LoteInsumo, Long> {

    List<LoteInsumo> findByInsumoIdAndEstado(Long insumoId, EstadoLote estado);

    List<LoteInsumo> findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
            Long insumoId,
            UbicacionInventario ubicacion,
            EstadoLote estado
    );

    Optional<LoteInsumo> findByInsumoIdAndUbicacionAndFechaVencimientoAndEstado(
            Long insumoId,
            UbicacionInventario ubicacion,
            LocalDateTime fechaVencimiento,
            EstadoLote estado
    );

    List<LoteInsumo> findByInsumoIdOrderByUbicacionAscFechaVencimientoAscFechaIngresoAsc(Long insumoId);

    List<LoteInsumo> findByInsumoIdAndEstadoOrderByUbicacionAscFechaVencimientoAscFechaIngresoAsc(
            Long insumoId,
            EstadoLote estado
    );
}
