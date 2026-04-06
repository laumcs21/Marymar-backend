package com.marymar.app.business.Service;

import com.marymar.app.persistence.Entity.Auditoria;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaService {
    void registrar(String accion, String entidad, Long entidadId, String detalle, String usuarioManual);

    List<Auditoria> obtenerTodosOrdenados();

    List<Auditoria> filtrar(
            String usuario,
            String accion,
            String entidad,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );
}
