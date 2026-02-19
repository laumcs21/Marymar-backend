package com.marymar.app.persistence.Mapper;

import com.marymar.app.persistence.Entity.Factura;
import com.marymar.app.business.DTO.FacturaResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FacturaMapper {

    public FacturaResponseDTO toResponseDTO(Factura factura) {

        if (factura == null) {
            return null;
        }

        return new FacturaResponseDTO(
                factura.getId(),
                factura.getPedido().getId(),
                factura.getPedido().getCliente().getNombre(),
                factura.getFecha(),
                factura.getTotal()
        );
    }
}



