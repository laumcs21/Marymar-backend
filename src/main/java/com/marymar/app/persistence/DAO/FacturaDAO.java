package com.marymar.app.persistence.DAO;

import com.marymar.app.persistence.Entity.Factura;
import com.marymar.app.persistence.Mapper.FacturaMapper;
import com.marymar.app.persistence.Repository.FacturaRepository;
import com.marymar.app.business.DTO.FacturaResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FacturaDAO {

    private final FacturaRepository facturaRepository;
    private final FacturaMapper facturaMapper;

    public FacturaDAO(FacturaRepository facturaRepository,
                      FacturaMapper facturaMapper) {
        this.facturaRepository = facturaRepository;
        this.facturaMapper = facturaMapper;
    }

    /**
     * Guarda una factura
     */
    public FacturaResponseDTO save(Factura factura) {

        Factura facturaGuardada = facturaRepository.save(factura);

        return facturaMapper.toResponseDTO(facturaGuardada);
    }

    /**
     * Busca factura por ID
     */
    public Optional<FacturaResponseDTO> findById(Long id) {

        return facturaRepository.findById(id)
                .map(facturaMapper::toResponseDTO);
    }

    /**
     * Busca factura por ID del pedido
     */
    public Optional<FacturaResponseDTO> findByPedidoId(Long pedidoId) {

        return facturaRepository.findByPedidoId(pedidoId)
                .map(facturaMapper::toResponseDTO);
    }

    /**
     * Verifica si ya existe factura para un pedido
     */
    public boolean existsByPedidoId(Long pedidoId) {
        return facturaRepository.findByPedidoId(pedidoId).isPresent();
    }
}
