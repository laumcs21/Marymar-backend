package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

        Optional<Inventario> findByInsumoId(Long insumoId);

}

