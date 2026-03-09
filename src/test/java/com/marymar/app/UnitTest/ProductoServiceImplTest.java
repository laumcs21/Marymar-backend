package com.marymar.app.UnitTest;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.business.Service.ImageService;
import com.marymar.app.business.Service.impl.ProductoServiceImpl;
import com.marymar.app.persistence.DAO.CategoriaDAO;
import com.marymar.app.persistence.DAO.ProductoDAO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.persistence.Entity.ProductoImagen;
import com.marymar.app.persistence.Mapper.ProductoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoDAO productoDAO;
    @Mock
    private CategoriaDAO categoriaDAO;
    @Mock
    private ImageService imageService;
    @Mock
    private ProductoMapper productoMapper;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private ProductoCreateDTO dto;
    private Categoria categoria;
    private Producto producto;

    @BeforeEach
    void setUp() {
        dto = new ProductoCreateDTO("Mojarra", new BigDecimal("35000"), 1L, "Mojarra frita");
        categoria = new Categoria("Pescados");
        producto = new Producto("Mojarra", new BigDecimal("35000"), categoria, "Mojarra frita");
        producto.setId(10L);
        producto.setImagenes(new ArrayList<>());
    }

    @Test
    void deberiaCrearProductoCorrectamenteSinImagenes() throws Exception {
        ProductoResponseDTO response = new ProductoResponseDTO(10L, "Mojarra", new BigDecimal("35000"),
                "Mojarra frita", 1L, "Pescados", true);
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoMapper.toEntity(dto, categoria)).thenReturn(producto);
        when(productoDAO.guardarEntidad(producto)).thenReturn(producto);
        when(productoMapper.toDTO(producto)).thenReturn(response);

        ProductoResponseDTO resultado = productoService.crear(dto, null);

        assertEquals("Mojarra", resultado.getNombre());
        verify(productoDAO).guardarEntidad(producto);
        verify(imageService, never()).uploadImage(any(), anyString(), anyString());
    }

    @Test
    void deberiaCrearProductoConImagenesYMarcarPrimeraComoPrincipal() throws Exception {
        MockMultipartFile img1 = new MockMultipartFile("imagenes", "a.jpg", "image/jpeg", "img1".getBytes());
        MockMultipartFile img2 = new MockMultipartFile("imagenes", "b.jpg", "image/jpeg", "img2".getBytes());

        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoMapper.toEntity(dto, categoria)).thenReturn(producto);
        when(productoDAO.guardarEntidad(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(imageService.uploadImage(eq(img1), eq("productos"), anyString()))
                .thenReturn(new ImageService.Upload("http://img/1.jpg", "p1", "jpg"));
        when(imageService.uploadImage(eq(img2), eq("productos"), anyString()))
                .thenReturn(new ImageService.Upload("http://img/2.jpg", "p2", "jpg"));
        when(productoMapper.toDTO(any(Producto.class))).thenAnswer(invocation -> {
            Producto guardado = invocation.getArgument(0);
            return new ProductoResponseDTO(
                    guardado.getId(),
                    guardado.getNombre(),
                    guardado.getPrecio(),
                    guardado.getDescripcion(),
                    1L,
                    "Pescados",
                    true,
                    guardado.getImagenes().stream().map(ProductoImagen::getUrl).toList(),
                    guardado.getImagenes().isEmpty() ? null : guardado.getImagenes().get(0).getUrl()
            );
        });

        ProductoResponseDTO resultado = productoService.crear(dto, List.of(img1, img2));

        assertEquals(2, resultado.getImagenesUrls().size());
        assertEquals("http://img/1.jpg", resultado.getImagenPrincipal());
        assertTrue(producto.getImagenes().get(0).getPrincipal());
        assertFalse(producto.getImagenes().get(1).getPrincipal());
        verify(productoDAO, times(2)).guardarEntidad(any(Producto.class));
    }

    @Test
    void deberiaIgnorarImagenesVaciasAlCrear() throws Exception {
        MultipartFile vacia = new MockMultipartFile("imagenes", new byte[0]);
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoMapper.toEntity(dto, categoria)).thenReturn(producto);
        when(productoDAO.guardarEntidad(any(Producto.class))).thenReturn(producto);
        when(productoMapper.toDTO(producto)).thenReturn(new ProductoResponseDTO(10L, "Mojarra",
                new BigDecimal("35000"), "Mojarra frita", 1L, "Pescados", true));

        productoService.crear(dto, List.of(vacia));

        verify(imageService, never()).uploadImage(any(), anyString(), anyString());
        assertTrue(producto.getImagenes().isEmpty());
    }

    @Test
    void deberiaFallarSiSubidaDeImagenLanzaErrorEnCrear() throws Exception {
        MockMultipartFile img1 = new MockMultipartFile("imagenes", "a.jpg", "image/jpeg", "img1".getBytes());
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoMapper.toEntity(dto, categoria)).thenReturn(producto);
        when(productoDAO.guardarEntidad(any(Producto.class))).thenReturn(producto);
        when(imageService.uploadImage(eq(img1), eq("productos"), anyString()))
                .thenThrow(new RuntimeException("cloudinary error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.crear(dto, List.of(img1)));

        assertEquals("Error subiendo la imagen", ex.getMessage());
    }

    @Test
    void obtenerOperacionesDeberianDelegarEnDao() {
        ProductoResponseDTO response = new ProductoResponseDTO(10L, "Mojarra", new BigDecimal("35000"),
                "Mojarra frita", 1L, "Pescados", true);
        when(productoDAO.obtenerPorId(10L)).thenReturn(response);
        when(productoDAO.obtenerTodos()).thenReturn(List.of(response));

        assertSame(response, productoService.obtenerPorId(10L));
        assertEquals(1, productoService.obtenerTodos().size());
    }

    @Test
    void actualizarDeberiaFallarSiProductoEstaInactivo() {
        producto.setActivo(false);
        when(productoDAO.obtenerEntidadPorId(10L)).thenReturn(producto);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> productoService.actualizar(10L, dto, null));

        assertEquals("No se puede modificar un producto inactivo", ex.getMessage());
    }

    @Test
    void actualizarDeberiaModificarSinImagenes() {
        ProductoResponseDTO response = new ProductoResponseDTO(10L, "Mojarra", new BigDecimal("35000"),
                "Mojarra frita", 1L, "Pescados", true);
        when(productoDAO.obtenerEntidadPorId(10L)).thenReturn(producto);
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoDAO.guardarEntidad(producto)).thenReturn(producto);
        when(productoMapper.toDTO(producto)).thenReturn(response);

        ProductoResponseDTO resultado = productoService.actualizar(10L, dto, null);

        assertEquals("Mojarra", resultado.getNombre());
        verify(productoMapper).updateFromDTO(producto, dto, categoria);
    }

    @Test
    void actualizarDeberiaEliminarImagenesAnterioresYSubirNuevas() throws Exception {
        ProductoImagen actual = new ProductoImagen();
        actual.setPublicId("old-public");
        actual.setUrl("http://old.jpg");
        producto.setImagenes(new ArrayList<>(List.of(actual)));
        MockMultipartFile nueva = new MockMultipartFile("imagenes", "nueva.jpg", "image/jpeg", "img".getBytes());
        when(productoDAO.obtenerEntidadPorId(10L)).thenReturn(producto);
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoDAO.guardarEntidad(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(imageService.uploadImage(eq(nueva), eq("productos"), anyString()))
                .thenReturn(new ImageService.Upload("http://new.jpg", "new-public", "jpg"));
        when(productoMapper.toDTO(any(Producto.class))).thenAnswer(invocation -> {
            Producto guardado = invocation.getArgument(0);
            return new ProductoResponseDTO(
                    guardado.getId(), guardado.getNombre(), guardado.getPrecio(), guardado.getDescripcion(),
                    1L, "Pescados", true,
                    guardado.getImagenes().stream().map(ProductoImagen::getUrl).toList(),
                    guardado.getImagenes().get(0).getUrl());
        });

        ProductoResponseDTO resultado = productoService.actualizar(10L, dto, List.of(nueva));

        verify(imageService).deleteByPublicId("old-public");
        verify(imageService).uploadImage(eq(nueva), eq("productos"), anyString());
        assertEquals(1, resultado.getImagenesUrls().size());
        assertEquals("http://new.jpg", resultado.getImagenPrincipal());
    }

    @Test
    void actualizarDeberiaFallarSiNoPuedeEliminarImagenAnterior() throws Exception {
        ProductoImagen actual = new ProductoImagen();
        actual.setPublicId("old-public");
        producto.setImagenes(new ArrayList<>(List.of(actual)));
        MockMultipartFile nueva = new MockMultipartFile("imagenes", "nueva.jpg", "image/jpeg", "img".getBytes());
        when(productoDAO.obtenerEntidadPorId(10L)).thenReturn(producto);
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoDAO.guardarEntidad(any(Producto.class))).thenReturn(producto);
        doThrow(new RuntimeException("delete error")).when(imageService).deleteByPublicId("old-public");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productoService.actualizar(10L, dto, List.of(nueva)));

        assertEquals("Error eliminando la imagen", ex.getMessage());
    }

    @Test
    void actualizarDeberiaFallarSiSubidaDeNuevaImagenLanzaError() throws Exception {
        producto.setImagenes(new ArrayList<>());
        MockMultipartFile nueva = new MockMultipartFile("imagenes", "nueva.jpg", "image/jpeg", "img".getBytes());
        when(productoDAO.obtenerEntidadPorId(10L)).thenReturn(producto);
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoDAO.guardarEntidad(any(Producto.class))).thenReturn(producto);
        when(imageService.uploadImage(eq(nueva), eq("productos"), anyString()))
                .thenThrow(new RuntimeException("upload error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.actualizar(10L, dto, List.of(nueva)));

        assertEquals("Error subiendo la imagen", ex.getMessage());
    }

    @Test
    void desactivarDeberiaAlternarEstado() {
        producto.setActivo(true);
        when(productoDAO.obtenerEntidadPorId(10L)).thenReturn(producto);

        productoService.desactivar(10L);
        assertFalse(producto.isActivo());

        productoService.desactivar(10L);
        assertTrue(producto.isActivo());
    }

    @Test
    void obtenerPorCategoriaDeberiaValidarCategoriaYDelegar() {
        ProductoResponseDTO response = new ProductoResponseDTO(10L, "Mojarra", new BigDecimal("35000"),
                "Mojarra frita", 1L, "Pescados", true);
        when(categoriaDAO.obtenerEntidadPorId(1L)).thenReturn(categoria);
        when(productoDAO.obtenerPorCategoria(1L)).thenReturn(List.of(response));

        List<ProductoResponseDTO> resultado = productoService.obtenerPorCategoria(1L);

        assertEquals(1, resultado.size());
        verify(categoriaDAO).obtenerEntidadPorId(1L);
    }

    @Test
    void eliminarDefinitivoDeberiaEliminarImagenesYProducto() throws Exception {
        ProductoImagen img1 = new ProductoImagen();
        img1.setUrl("https://res.cloudinary.com/demo/image/upload/v123/marymar/productos/a.jpg");
        ProductoImagen img2 = new ProductoImagen();
        img2.setUrl("https://res.cloudinary.com/demo/image/upload/v123/marymar/productos/b.jpg");
        producto.setImagenes(List.of(img1, img2));
        when(productoDAO.obtenerEntidadPorId(10L)).thenReturn(producto);
        when(imageService.tryExtractPublicId(img1.getUrl())).thenReturn("marymar/productos/a");
        when(imageService.tryExtractPublicId(img2.getUrl())).thenReturn("marymar/productos/b");

        productoService.eliminarDefinitivo(10L);

        verify(imageService).deleteByPublicId("marymar/productos/a");
        verify(imageService).deleteByPublicId("marymar/productos/b");
        verify(productoDAO).eliminarDefinitivo(10L);
    }

    @Test
    void eliminarDefinitivoDeberiaFallarSiCloudinaryFalla() throws Exception {
        ProductoImagen img1 = new ProductoImagen();
        img1.setUrl("https://res.cloudinary.com/demo/image/upload/v123/marymar/productos/a.jpg");
        producto.setImagenes(List.of(img1));
        when(productoDAO.obtenerEntidadPorId(10L)).thenReturn(producto);
        when(imageService.tryExtractPublicId(img1.getUrl())).thenReturn("marymar/productos/a");
        doThrow(new RuntimeException("cloudinary error")).when(imageService).deleteByPublicId("marymar/productos/a");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.eliminarDefinitivo(10L));

        assertEquals("Error eliminando imagen de Cloudinary", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirPrecioNegativo() {
        dto.setPrecio(new BigDecimal("-1000"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productoService.crear(dto, null));

        assertEquals("El precio debe ser mayor a 0", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirNombreVacio() {
        dto.setNombre(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productoService.crear(dto, null));

        assertEquals("El nombre del producto es obligatorio", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirCategoriaNula() {
        dto.setCategoriaId(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productoService.crear(dto, null));

        assertEquals("La categoría es obligatoria", ex.getMessage());
    }

    @Test
    void noDeberiaPermitirDescripcionVacia() {
        dto.setDescripcion(" ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> productoService.crear(dto, null));

        assertEquals("La descripción es obligatoria", ex.getMessage());
    }
}
