package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.InsumoCreateDTO;
import com.marymar.app.business.DTO.InsumoResponseDTO;
import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.Service.GoogleIdTokenService;
import com.marymar.app.business.Service.InsumoService;
import com.marymar.app.business.Service.InventarioService;
import com.marymar.app.persistence.Repository.InsumoRepository;
import com.marymar.app.persistence.Repository.InventarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class InsumoServiceIntegrationTest {

    @Autowired private InsumoService insumoService;
    @Autowired private InventarioService inventarioService;
    @Autowired private InsumoRepository insumoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private EntityManager entityManager;
    @MockitoBean
    private GoogleIdTokenService googleIdTokenService;

    @Test
    void deberiaCrearYActualizarInsumoCorrectamente() {
        InsumoResponseDTO creado = insumoService.crear(new InsumoCreateDTO("Harina", "kg"));
        assertNotNull(creado.getId());
        assertEquals("Harina", creado.getNombre());

        InsumoResponseDTO actualizado = insumoService.actualizar(creado.getId(), new InsumoCreateDTO("Harina premium", "g"));
        entityManager.flush();
        entityManager.clear();

        assertEquals("Harina premium", actualizado.getNombre());
        assertEquals("g", actualizado.getUnidad());
        assertEquals("Harina premium", insumoRepository.findById(creado.getId()).orElseThrow().getNombre());
    }

    @Test
    void noDeberiaPermitirCrearInsumoDuplicado() {
        insumoService.crear(new InsumoCreateDTO("Arroz", "kg"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> insumoService.crear(new InsumoCreateDTO("Arroz", "kg")));

        assertEquals("El insumo ya existe", ex.getMessage());
    }

    @Test
    void eliminarDeberiaBorrarTambienInventarioAsociado() {
        InsumoResponseDTO insumo = insumoService.crear(new InsumoCreateDTO("Aceite", "lt"));
        inventarioService.crear(new InventarioCreateDTO(insumo.getId(), 8));
        assertTrue(inventarioRepository.findByInsumoId(insumo.getId()).isPresent());

        insumoService.eliminar(insumo.getId());
        entityManager.flush();
        entityManager.clear();

        assertTrue(insumoRepository.findById(insumo.getId()).isEmpty());
        assertTrue(inventarioRepository.findByInsumoId(insumo.getId()).isEmpty());
    }
}
