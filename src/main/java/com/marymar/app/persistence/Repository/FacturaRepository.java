package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Long> {

    Optional<Factura> findByPedidoId(Long pedidoId);

}

