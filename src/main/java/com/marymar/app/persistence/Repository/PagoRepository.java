package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByPedidoId(Long pedidoId);

    List<Pago> findByComprobanteUrlIsNotNull();

    List<Pago> findByPedidoIdAndComprobanteUrlIsNotNull(Long pedidoId);

    List<Pago> findByPedidoIdAndComprobanteUrlIsNotNullAndFechaPagoBetween(Long pedidoId, LocalDateTime inicio, LocalDateTime fin);
}