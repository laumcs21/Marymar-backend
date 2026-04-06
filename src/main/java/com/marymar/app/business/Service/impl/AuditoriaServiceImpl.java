package com.marymar.app.business.Service.impl;

import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.persistence.Entity.Auditoria;
import com.marymar.app.persistence.Repository.AuditoriaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaServiceImpl(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    public void registrar(String accion, String entidad, Long entidadId, String detalle, String usuarioManual) {

        String usuario;

        if (usuarioManual != null) {
            usuario = usuarioManual;
        } else {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            usuario = (auth != null) ? auth.getName() : "anonymous";
        }

        Auditoria log = new Auditoria();
        log.setUsuario(usuario);
        log.setAccion(accion);
        log.setEntidad(entidad);
        log.setEntidadId(entidadId);
        log.setDetalle(detalle);
        log.setFecha(LocalDateTime.now());

        auditoriaRepository.save(log);
    }

    @Override
    public List<Auditoria> obtenerTodosOrdenados() {
        return auditoriaRepository.findAllByOrderByFechaDesc();
    }

    @Override
    public List<Auditoria> filtrar(
            String usuario,
            String accion,
            String entidad,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {
        return auditoriaRepository.filtrar(
                usuario,
                accion,
                entidad,
                fechaInicio,
                fechaFin
        );
    }
}
