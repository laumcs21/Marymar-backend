package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.EstadoPedido;
import com.marymar.app.persistence.Entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteId(Long clienteId);

    Optional<Pedido> findByEstado(Long mesaId, EstadoPedido estado);

    Optional<Pedido> findFirstByMesaIdAndEstadoNotIn(Long mesaId, List<EstadoPedido> estados);

    List<Pedido> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Pedido> findByFechaAfter(LocalDateTime inicio);

    List<Pedido> findByFechaBefore(LocalDateTime fin);
}

