package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.VerificacionCodigo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificacionCodigoRepository extends JpaRepository<VerificacionCodigo, Long> {
        Optional<VerificacionCodigo> findByEmailAndCodeAndUsedFalse(String email, String code);
    List<VerificacionCodigo> findByEmailAndUsedFalse(String email);
}

