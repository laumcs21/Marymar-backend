package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

        Optional<Inventario> findByInsumoId(Long insumoId);

        @Transactional
        @Modifying
        @Query("DELETE FROM Inventario i WHERE i.insumo.id = :insumoId")
        void deleteByInsumoId(Long insumoId);}

