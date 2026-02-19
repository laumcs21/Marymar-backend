package com.marymar.app.persistence.Repository;

import com.marymar.app.persistence.Entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedidoId(Integer pedidoId);

}
