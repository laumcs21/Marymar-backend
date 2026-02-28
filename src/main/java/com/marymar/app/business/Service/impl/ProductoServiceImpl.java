package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.business.Service.ImageService;
import com.marymar.app.persistence.DAO.CategoriaDAO;
import com.marymar.app.persistence.DAO.ProductoDAO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.business.Service.ProductoService;
import com.marymar.app.persistence.Entity.ProductoImagen;
import com.marymar.app.persistence.Mapper.ProductoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;
    private final ImageService imageService;
    private final ProductoMapper productoMapper;

    public ProductoServiceImpl(ProductoDAO productoDAO,
                               CategoriaDAO categoriaDAO, ImageService imageService, ProductoMapper productoMapper) {
        this.productoDAO = productoDAO;
        this.categoriaDAO = categoriaDAO;
        this.imageService = imageService;
        this.productoMapper = productoMapper;
    }

    // =========================
    // CREAR
    // =========================
    @Override
    public ProductoResponseDTO crear(ProductoCreateDTO dto,
                                     List<MultipartFile> imagenes) {

        validarProducto(dto);

        Categoria categoria = categoriaDAO.obtenerEntidadPorId(dto.getCategoriaId());

        Producto producto = productoMapper.toEntity(dto, categoria);

        producto = productoDAO.guardarEntidad(producto);

        if (imagenes != null && !imagenes.isEmpty()) {

            int orden = 0;

            for (MultipartFile imagen : imagenes) {

                if (imagen.isEmpty()) continue;

                ImageService.Upload upload;

                try {
                    upload = imageService.uploadImage(
                            imagen,
                            "productos",
                            "producto_" + System.currentTimeMillis() + "_" + orden
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Error subiendo la imagen", e);
                }

                ProductoImagen productoImagen = new ProductoImagen();
                productoImagen.setUrl(upload.getUrl());
                productoImagen.setPublicId(upload.getPublicId());
                productoImagen.setProducto(producto);
                productoImagen.setPrincipal(orden == 0);
                productoImagen.setOrden(orden);

                producto.getImagenes().add(productoImagen);

                orden++;
            }

            producto = productoDAO.guardarEntidad(producto);
        }

        return productoMapper.toDTO(producto);
    }
    // =========================
    // OBTENER POR ID
    // =========================
    @Override
    public ProductoResponseDTO obtenerPorId(Long id) {
        return productoDAO.obtenerPorId(id);
    }

    // =========================
    // OBTENER TODOS
    // =========================
    @Override
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoDAO.obtenerTodos();
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @Override
    public ProductoResponseDTO actualizar(Long id,
                                          ProductoCreateDTO dto,
                                          List<MultipartFile> imagenes) {

        Producto producto = productoDAO.obtenerEntidadPorId(id);

        if (!producto.isActivo()) {
            throw new IllegalStateException("No se puede modificar un producto inactivo");
        }

        validarProducto(dto);

        Categoria categoria = categoriaDAO.obtenerEntidadPorId(dto.getCategoriaId());

        // 1️⃣ Actualizar datos básicos
        productoMapper.updateFromDTO(producto, dto, categoria);

        // 2️⃣ Guardar primero para que Hibernate sincronice correctamente
        producto = productoDAO.guardarEntidad(producto);

        // 3️⃣ Ahora agregar imágenes nuevas (sin tocar las existentes)
        if (imagenes != null && !imagenes.isEmpty()) {

            int ordenInicial = producto.getImagenes() != null
                    ? producto.getImagenes().size()
                    : 0;

            for (MultipartFile imagen : imagenes) {

                if (imagen.isEmpty()) continue;

                ImageService.Upload upload;

                try {
                    upload = imageService.uploadImage(
                            imagen,
                            "productos",
                            "producto_" + System.currentTimeMillis() + "_" + ordenInicial
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Error subiendo la imagen", e);
                }

                ProductoImagen productoImagen = new ProductoImagen();
                productoImagen.setUrl(upload.getUrl());
                productoImagen.setPublicId(upload.getPublicId());
                productoImagen.setProducto(producto);
                productoImagen.setPrincipal(producto.getImagenes().isEmpty());
                productoImagen.setOrden(ordenInicial);

                producto.getImagenes().add(productoImagen);

                ordenInicial++;
            }

            // 4️⃣ Guardar nuevamente con las imágenes agregadas
            producto = productoDAO.guardarEntidad(producto);
        }

        return productoMapper.toDTO(producto);
    }
    // =========================
    // DESACTIVAR
    // =========================
    @Override
    public void desactivar(Long id) {

        Producto producto = productoDAO.obtenerEntidadPorId(id);

        producto.setActivo(!producto.isActivo());
    }

    @Override
    public List<ProductoResponseDTO> obtenerPorCategoria(Long categoriaId) {

        categoriaDAO.obtenerEntidadPorId(categoriaId);

        return productoDAO.obtenerPorCategoria(categoriaId);
    }

    @Override
    public void eliminarDefinitivo(Long id) {

        Producto producto = productoDAO.obtenerEntidadPorId(id);

        if (producto.getImagenes() != null) {
            producto.getImagenes().forEach(img -> {
                try {
                    String publicId = imageService.tryExtractPublicId(img.getUrl());
                    if (publicId != null) {
                        imageService.deleteByPublicId(publicId);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error eliminando imagen de Cloudinary", e);
                }
            });
        }

        productoDAO.eliminarDefinitivo(id);
    }

    // =========================
    // VALIDACIONES
    // =========================
    private void validarProducto(ProductoCreateDTO dto) {

        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }

        if (dto.getPrecio() == null || dto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0");
        }

        if (dto.getCategoriaId() == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }

        if (dto.getDescripcion() == null || dto.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
    }
}
