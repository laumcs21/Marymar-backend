package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import com.marymar.app.persistence.DAO.CategoriaDAO;
import com.marymar.app.persistence.DAO.ProductoDAO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.persistence.Entity.Producto;
import com.marymar.app.business.Service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;

    public ProductoServiceImpl(ProductoDAO productoDAO,
                               CategoriaDAO categoriaDAO) {
        this.productoDAO = productoDAO;
        this.categoriaDAO = categoriaDAO;
    }

    // =========================
    // CREAR
    // =========================
    @Override
    public ProductoResponseDTO crear(ProductoCreateDTO dto) {

        validarProducto(dto);

        Categoria categoria = categoriaDAO.obtenerEntidadPorId(dto.getCategoriaId());

        return productoDAO.crear(dto, categoria);
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
    public ProductoResponseDTO actualizar(Long id, ProductoCreateDTO dto) {

        Producto producto = productoDAO.obtenerEntidadPorId(id);

        if (!producto.isActivo()) {
            throw new IllegalStateException("No se puede modificar un producto inactivo");
        }

        validarProducto(dto);

        Categoria categoria = categoriaDAO.obtenerEntidadPorId(dto.getCategoriaId());

        return productoDAO.actualizar(id, dto, categoria);
    }

    // =========================
    // DESACTIVAR
    // =========================
    @Override
    public void desactivar(Long id) {

        Producto producto = productoDAO.obtenerEntidadPorId(id);

        if (!producto.isActivo()) {
            throw new IllegalStateException("El producto ya está inactivo");
        }

        productoDAO.desactivar(id);
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
    }
}
