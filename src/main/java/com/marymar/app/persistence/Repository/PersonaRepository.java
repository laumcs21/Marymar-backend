package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByEmail(String email);
    Optional<Persona> existsByNumeroIdentificacion (String numeroIdentificacion);
    boolean existsByEmailAndIdNot(String email, Long id);
    Optional<Persona> findByResetToken (String resetToken);
}

