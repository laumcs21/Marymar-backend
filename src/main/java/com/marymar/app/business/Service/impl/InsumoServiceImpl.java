package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.InsumoCreateDTO;
import com.marymar.app.business.DTO.InsumoResponseDTO;
import com.marymar.app.business.Service.InsumoService;
import com.marymar.app.persistence.DAO.InsumoDAO;
import com.marymar.app.persistence.Entity.Insumo;
import com.marymar.app.persistence.Repository.InsumoRepository;
import com.marymar.app.persistence.Repository.InventarioRepository;
import com.marymar.app.persistence.Repository.ProductoInsumoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InsumoServiceImpl implements InsumoService {

    private final InsumoDAO insumoDAO;
    private final InsumoRepository insumoRepository;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final InventarioRepository inventarioRepository;

    public InsumoServiceImpl(InsumoDAO insumoDAO, InsumoRepository insumoRepository, ProductoInsumoRepository productoInsumoRepository, InventarioRepository inventarioRepository) {
        this.insumoDAO = insumoDAO;
        this.insumoRepository = insumoRepository;
        this.productoInsumoRepository = productoInsumoRepository;
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    public InsumoResponseDTO crear(InsumoCreateDTO dto) {

        if(dto.getNombre() == null || dto.getNombre().isBlank()){
            throw new IllegalArgumentException("El nombre del insumo es obligatorio");
        }

        if(insumoRepository.findByNombre(dto.getNombre()).isPresent()){
            throw new IllegalArgumentException("El insumo ya existe");
        }

        return insumoDAO.crear(dto);
    }

    @Override
    public InsumoResponseDTO obtenerPorId(Long id) {
        return insumoDAO.obtenerPorId(id);
    }

    @Override
    public List<InsumoResponseDTO> obtenerTodos() {
        return insumoDAO.obtenerTodos();
    }

    @Override
    public void eliminar(Long id) {

        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        boolean usadoEnReceta = productoInsumoRepository.existsByInsumoId(id);

        if(usadoEnReceta){
            throw new IllegalStateException(
                    "No se puede eliminar el insumo porque está asociado a productos"
            );
        }

        // ELIMINAR INVENTARIO
        inventarioRepository.deleteByInsumoId(id);

        // ELIMINAR INSUMO
        insumoRepository.delete(insumo);
    }

    @Override
    public InsumoResponseDTO actualizar(Long id, InsumoCreateDTO dto) {

        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        if(dto.getNombre() == null || dto.getNombre().isBlank()){
            throw new IllegalArgumentException("El nombre del insumo es obligatorio");
        }

        // Verificar duplicado (pero permitiendo el mismo registro)
        insumoRepository.findByNombre(dto.getNombre())
                .ifPresent(i -> {
                    if(!i.getId().equals(id)){
                        throw new IllegalArgumentException("Ya existe un insumo con ese nombre");
                    }
                });

        insumo.setNombre(dto.getNombre());
        insumo.setUnidad(dto.getUnidad());

        insumoRepository.save(insumo);

        return new InsumoResponseDTO(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getUnidad()
        );
    }
}