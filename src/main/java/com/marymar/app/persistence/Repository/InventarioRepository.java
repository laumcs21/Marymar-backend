package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.Inventario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

        @Transactional
        @Modifying
        @Query("DELETE FROM Inventario i WHERE i.insumo.id = :insumoId")
        void deleteByInsumoId(Long insumoId);

        @Query("SELECT i.stock FROM Inventario i WHERE i.insumo.id = :insumoId")
        Integer obtenerStock(Long insumoId);

        @Modifying
        @Query("UPDATE Inventario i SET i.stock = i.stock - :cantidad WHERE i.insumo.id = :insumoId")
        void descontarStock(Long insumoId, Integer cantidad);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        Optional<Inventario> findByInsumoId(Long insumoId);
}



