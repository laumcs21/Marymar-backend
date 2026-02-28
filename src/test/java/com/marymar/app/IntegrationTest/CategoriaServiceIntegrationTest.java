package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.business.Service.CategoriaService;
import com.marymar.app.persistence.Repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoriaServiceIntegrationTest {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // ===============================
    // CREACIÓN EXITOSA
    // ===============================

    @Test
    void deberiaCrearCategoriaCorrectamente() {

        CategoriaCreateDTO dto = new CategoriaCreateDTO();
        dto.setNombre("Bebidas");

        CategoriaResponseDTO creada = categoriaService.crear(dto);

        assertNotNull(creada.getId());
        assertEquals("Bebidas", creada.getNombre());

        var entidad = categoriaRepository.findById(creada.getId()).orElse(null);
        assertNotNull(entidad);
    }

    // ===============================
    // NO PERMITIR NOMBRE DUPLICADO
    // ===============================

    @Test
    void noDeberiaPermitirNombreDuplicado() {

        CategoriaCreateDTO dto1 = new CategoriaCreateDTO();
        dto1.setNombre("Postres");
        categoriaService.crear(dto1);

        CategoriaCreateDTO dto2 = new CategoriaCreateDTO();
        dto2.setNombre("Postres");

        assertThrows(RuntimeException.class, () -> {
            categoriaService.crear(dto2);
        });
    }

    // ===============================
    // NO PERMITIR NOMBRE VACÍO
    // ===============================

    @Test
    void noDeberiaPermitirNombreVacio() {

        CategoriaCreateDTO dto = new CategoriaCreateDTO();
        dto.setNombre("");

        assertThrows(RuntimeException.class, () -> {
            categoriaService.crear(dto);
        });
    }
}
