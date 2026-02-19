package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.CategoriaCreateDTO;
import com.marymar.app.business.DTO.CategoriaResponseDTO;
import com.marymar.app.persistence.DAO.CategoriaDAO;
import com.marymar.app.persistence.Entity.Categoria;
import com.marymar.app.business.Service.CategoriaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaDAO categoriaDAO;

    public CategoriaServiceImpl(CategoriaDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    // =========================
    // CREAR
    // =========================
    @Override
    public CategoriaResponseDTO crear(CategoriaCreateDTO dto) {

        validarNombre(dto.getNombre());

        if (categoriaDAO.existePorNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        return categoriaDAO.crear(dto);
    }

    // =========================
    // OBTENER POR ID
    // =========================
    @Override
    public CategoriaResponseDTO obtenerPorId(Long id) {
        return categoriaDAO.obtenerPorId(id);
    }

    // =========================
    // OBTENER TODAS
    // =========================
    @Override
    public List<CategoriaResponseDTO> obtenerTodas() {
        return categoriaDAO.obtenerTodas();
    }

    // =========================
    // ACTUALIZAR
    // =========================
    @Override
    public CategoriaResponseDTO actualizar(Long id, CategoriaCreateDTO dto) {

        validarNombre(dto.getNombre());

        Categoria categoriaExistente = categoriaDAO.obtenerEntidadPorId(id);

        if (!categoriaExistente.getNombre().equalsIgnoreCase(dto.getNombre())
                && categoriaDAO.existePorNombre(dto.getNombre())) {

            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }

        return categoriaDAO.actualizar(id, dto);
    }

    // =========================
    // ELIMINAR
    // =========================
    @Override
    public void eliminar(Long id) {

        Categoria categoria = categoriaDAO.obtenerEntidadPorId(id);

        if (categoria.getProductos() != null && !categoria.getProductos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene productos asociados");
        }

        categoriaDAO.eliminar(id);
    }

    // =========================
    // VALIDACIÓN
    // =========================
    private void validarNombre(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }

        if (nombre.length() < 3) {
            throw new IllegalArgumentException("El nombre debe tener mínimo 3 caracteres");
        }
    }
}
