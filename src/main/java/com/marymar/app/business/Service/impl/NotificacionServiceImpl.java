package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.NotificacionDTO;
import com.marymar.app.business.Service.NotificacionService;
import com.marymar.app.persistence.Entity.EstadoLote;
import com.marymar.app.persistence.Entity.Inventario;
import com.marymar.app.persistence.Entity.LoteInsumo;
import com.marymar.app.persistence.Entity.UbicacionInventario;
import com.marymar.app.persistence.Repository.InventarioRepository;
import com.marymar.app.persistence.Repository.LoteInsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private LoteInsumoRepository loteRepository;

    @Override
    public List<NotificacionDTO> generarNotificaciones() {

        List<NotificacionDTO> notificaciones = new ArrayList<>();

        List<Inventario> inventarios = inventarioRepository.findAll();

        List<String> bodegaBaja = new ArrayList<>();
        List<String> cocinaBaja = new ArrayList<>();
        List<String> porVencer = new ArrayList<>();

        for (Inventario inv : inventarios) {

            String nombre = inv.getInsumo().getNombre();

            List<LoteInsumo> lotes = loteRepository
                    .findByInsumoIdAndEstado(
                            inv.getInsumo().getId(),
                            EstadoLote.ACTIVO
                    );

            int stockBodega = 0;
            int stockCocina = 0;

            for (LoteInsumo l : lotes) {

                if (UbicacionInventario.BODEGA==l.getUbicacion()) {
                    stockBodega += l.getCantidadDisponible();
                }

                if (UbicacionInventario.COCINA==l.getUbicacion()) {
                    stockCocina += l.getCantidadDisponible();
                }

                long dias = ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        l.getFechaVencimiento()
                );

                if (dias <= 3 && dias >= 0) {
                    porVencer.add(nombre + " (vence en " + dias + " días)");
                }
            }

            if (stockBodega <= 10) {
                bodegaBaja.add(nombre + " (" + stockBodega + ")");
            }

            // 🟠 COCINA BAJA
            if (stockCocina <= 10) {
                cocinaBaja.add(nombre + " (" + stockCocina + ")");
            }
        }

        // ================== MENSAJES ==================

        if (!bodegaBaja.isEmpty()) {
            notificaciones.add(new NotificacionDTO(
                    "Stock bajo en bodega:\n" + String.join(", ", bodegaBaja),
                    "BODEGA_BAJA",
                    null
            ));
        }

        if (!cocinaBaja.isEmpty()) {
            notificaciones.add(new NotificacionDTO(
                    "Stock bajo en cocina (surtir desde bodega):\n" + String.join(", ", cocinaBaja),
                    "COCINA_BAJA",
                    null
            ));
        }

        if (!porVencer.isEmpty()) {
            notificaciones.add(new NotificacionDTO(
                    "Productos por vencer:\n" + String.join(", ", porVencer),
                    "POR_VENCER",
                    null
            ));
        }

        return notificaciones;
    }
}