package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.EstadoLote;
import com.marymar.app.persistence.Entity.LoteInsumo;
import com.marymar.app.persistence.Entity.UbicacionInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoteInsumoRepository extends JpaRepository<LoteInsumo, Long> {

    List<LoteInsumo> findByInsumoId(Long insumoId);

    List<LoteInsumo> findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
            Long insumoId,
            UbicacionInventario ubicacion,
            EstadoLote estado
    );
}
