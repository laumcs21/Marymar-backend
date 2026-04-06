package com.marymar.app.controller;

import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.persistence.Entity.Auditoria;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Auditoria> obtenerLogs() {
        return auditoriaService.obtenerTodosOrdenados();
    }

    @GetMapping("/filtrar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Auditoria> filtrar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin
    ) {

        LocalDateTime inicio = (fechaInicio != null)
                ? LocalDateTime.parse(fechaInicio)
                : null;

        LocalDateTime fin = (fechaFin != null)
                ? LocalDateTime.parse(fechaFin)
                : null;

        return auditoriaService.filtrar(usuario, accion, entidad, inicio, fin);
    }
}
