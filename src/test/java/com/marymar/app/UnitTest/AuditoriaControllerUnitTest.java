package com.marymar.app.UnitTest;

import com.marymar.app.TestSupport.TestDataFactory;
import com.marymar.app.business.Service.AuditoriaService;
import com.marymar.app.controller.AuditoriaController;
import com.marymar.app.persistence.Entity.Auditoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaControllerUnitTest {

    @Mock private AuditoriaService auditoriaService;
    private AuditoriaController controller;

    @BeforeEach
    void setUp() { controller = new AuditoriaController(auditoriaService); }

    @Test
    void obtenerLogsDeberiaDelegar() {
        when(auditoriaService.obtenerTodosOrdenados()).thenReturn(List.of(TestDataFactory.auditoria(1L, "laura@test.com", "LOGIN", "USUARIO")));

        List<Auditoria> resultado = controller.obtenerLogs();

        assertEquals(1, resultado.size());
        verify(auditoriaService).obtenerTodosOrdenados();
    }

    @Test
    void filtrarDeberiaParsearFechasYDelegar() {
        when(auditoriaService.filtrar(eq("laura@test.com"), eq("LOGIN"), eq("USUARIO"), any(), any()))
                .thenReturn(List.of(TestDataFactory.auditoria(1L, "laura@test.com", "LOGIN", "USUARIO")));

        List<Auditoria> resultado = controller.filtrar(
                "laura@test.com",
                "LOGIN",
                "USUARIO",
                "2026-04-10T10:15:30",
                "2026-04-10T12:15:30"
        );

        assertEquals(1, resultado.size());
        verify(auditoriaService).filtrar(
                eq("laura@test.com"),
                eq("LOGIN"),
                eq("USUARIO"),
                eq(LocalDateTime.parse("2026-04-10T10:15:30")),
                eq(LocalDateTime.parse("2026-04-10T12:15:30"))
        );
    }
}
