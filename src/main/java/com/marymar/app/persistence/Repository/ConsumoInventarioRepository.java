package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.ConsumoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumoInventarioRepository extends JpaRepository<ConsumoInventario, Long> {
}