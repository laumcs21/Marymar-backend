package com.marymar.app.persistence.Mapper;

import com.marymar.app.business.DTO.PagoResponseDTO;
import com.marymar.app.persistence.Entity.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public PagoResponseDTO toDTO(Pago pago) {

        if (pago == null) return null;

        return new PagoResponseDTO(
                pago.getId(),
                pago.getMetodo().name(),
                pago.getMonto(),
                pago.getFechaPago(),
                pago.getComprobanteUrl()
        );
    }
}