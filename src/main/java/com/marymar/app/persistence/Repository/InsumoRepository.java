package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    Optional<Insumo> findByNombre(String nombre);

}
