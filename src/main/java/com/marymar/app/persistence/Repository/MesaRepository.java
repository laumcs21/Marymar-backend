package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    Optional<Mesa> findByNumero(Integer numero);
    List<Mesa> findByActivaTrue();

    @Override
    List<Mesa> findAll();

    boolean existsByNumero(Integer numero);

    boolean existsByNumeroAndIdNot(Integer numero, Long id);
}