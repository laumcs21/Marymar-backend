package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.EstadoPedido;
import com.marymar.app.persistence.Entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteId(Long clienteId);

    Optional<Pedido> findByMesaIdAndEstado(Long mesaId, EstadoPedido estado);
}

