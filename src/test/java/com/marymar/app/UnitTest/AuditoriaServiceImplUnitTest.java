package com.marymar.app.UnitTest;

import com.marymar.app.business.Service.impl.AuditoriaServiceImpl;
import com.marymar.app.persistence.Entity.Auditoria;
import com.marymar.app.persistence.Repository.AuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceImplUnitTest {

    @Mock private AuditoriaRepository auditoriaRepository;
    private AuditoriaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuditoriaServiceImpl(auditoriaRepository);
        SecurityContextHolder.clearContext();
    }

    @Test
    void registrarDeberiaUsarUsuarioManualCuandoVieneInformado() {
        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);

        service.registrar("LOGIN", "USUARIO", 1L, "detalle", "manual@test.com");

        verify(auditoriaRepository).save(captor.capture());
        assertEquals("manual@test.com", captor.getValue().getUsuario());
        assertEquals("LOGIN", captor.getValue().getAccion());
        assertNotNull(captor.getValue().getFecha());
    }

    @Test
    void registrarDeberiaUsarUsuarioDeSecurityContextSiNoHayManual() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("auth@test.com", "secret")
        );
        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);

        service.registrar("ACCION", "ENTIDAD", 5L, "detalle", null);

        verify(auditoriaRepository).save(captor.capture());
        assertEquals("auth@test.com", captor.getValue().getUsuario());
    }

    @Test
    void filtrarYObtenerTodosDeberianDelegar() {
        when(auditoriaRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(new Auditoria()));
        when(auditoriaRepository.filtrar(any(), any(), any(), any(), any())).thenReturn(List.of(new Auditoria()));

        assertEquals(1, service.obtenerTodosOrdenados().size());
        assertEquals(1, service.filtrar("u", "a", "e", LocalDateTime.now(), LocalDateTime.now()).size());
    }
}
