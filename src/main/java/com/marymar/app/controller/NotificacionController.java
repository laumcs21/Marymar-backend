package com.marymar.app.controller;

import com.marymar.app.business.DTO.NotificacionDTO;
import com.marymar.app.business.Service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public List<NotificacionDTO> obtenerNotificaciones() {
        return notificacionService.generarNotificaciones();
    }
}
