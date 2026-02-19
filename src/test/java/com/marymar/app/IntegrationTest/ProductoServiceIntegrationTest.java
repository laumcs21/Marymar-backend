package com.marymar.app.IntegrationTest;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.business.Service.CategoriaService;
import com.marymar.app.business.Service.ProductoService;
import com.marymar.app.persistence.Repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductoServiceIntegrationTest {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProductoRepository productoRepository;

    // ====================================
    // CREACIÓN EXITOSA
    // ====================================

    @Test
    void deberiaCrearProductoCorrectamente() {

        // Crear categoría primero
        CategoriaCreateDTO categoriaDTO = new CategoriaCreateDTO();
        categoriaDTO.setNombre("Ejecutivo");
        var categoria = categoriaService.crear(categoriaDTO);

        ProductoCreateDTO dto = new ProductoCreateDTO();
        dto.setNombre("Mojarra");
        dto.setPrecio(new BigDecimal("25000"));
        dto.setCategoriaId(categoria.getId());

        ProductoResponseDTO creado = productoService.crear(dto);

        assertNotNull(creado.getId());
        assertEquals("Mojarra", creado.getNombre());

        var entidad = productoRepository.findById(creado.getId()).orElse(null);
        assertNotNull(entidad);
    }

    // ====================================
    // NO PERMITIR PRECIO NEGATIVO
    // ====================================

    @Test
    void noDeberiaPermitirPrecioNegativo() {

        CategoriaCreateDTO categoriaDTO = new CategoriaCreateDTO();
        categoriaDTO.setNombre("Ejecutivo");
        var categoria = categoriaService.crear(categoriaDTO);

        ProductoCreateDTO dto = new ProductoCreateDTO();
        dto.setNombre("Mojarra");
        dto.setPrecio(new BigDecimal("-1000"));
        dto.setCategoriaId(categoria.getId());
        assertThrows(RuntimeException.class, () -> {
            productoService.crear(dto);
        });
    }

    // ====================================
    // NO PERMITIR CATEGORIA INEXISTENTE
    // ====================================

    @Test
    void noDeberiaPermitirCategoriaInexistente() {

        ProductoCreateDTO dto = new ProductoCreateDTO();
        dto.setNombre("Platos especiales");
        dto.setPrecio(new BigDecimal("30000"));
        dto.setCategoriaId(999L);

        assertThrows(RuntimeException.class, () -> {
            productoService.crear(dto);
        });
    }
}

