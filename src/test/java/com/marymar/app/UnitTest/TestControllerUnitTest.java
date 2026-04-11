package com.marymar.app.UnitTest;

import com.marymar.app.controller.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class TestControllerUnitTest {

    @Test
    void secureEndpointDeberiaRetornarMensajeEsperado() {
        TestController controller = new TestController();

        ResponseEntity<String> resultado = controller.secureEndpoint();

        assertEquals(200, resultado.getStatusCode().value());
        assertEquals("Acceso permitido 🔥", resultado.getBody());
    }
}
