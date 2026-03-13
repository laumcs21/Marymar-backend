package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.business.DTO.InventarioUpdateDTO;
import com.marymar.app.business.Service.InventarioService;
import com.marymar.app.persistence.Entity.Insumo;
import com.marymar.app.persistence.Entity.Inventario;
import com.marymar.app.persistence.Entity.ProductoInsumo;
import com.marymar.app.persistence.Mapper.InventarioMapper;
import com.marymar.app.persistence.Repository.InsumoRepository;
import com.marymar.app.persistence.Repository.InventarioRepository;
import com.marymar.app.persistence.Repository.ProductoInsumoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final InsumoRepository insumoRepository;
    private final InventarioMapper inventarioMapper;
    private final ProductoInsumoRepository productoInsumoRepository;

    public InventarioServiceImpl(
            InventarioRepository inventarioRepository,
            InsumoRepository insumoRepository,
            InventarioMapper inventarioMapper, ProductoInsumoRepository productoInsumoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.insumoRepository = insumoRepository;
        this.inventarioMapper = inventarioMapper;
        this.productoInsumoRepository = productoInsumoRepository;
    }

    @Override
    public InventarioResponseDTO crear(InventarioCreateDTO dto) {

        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        if(dto.getStock() < 0){
            throw new RuntimeException("El stock no puede ser negativo");
        }

        Optional<Inventario> inventarioExistente =
                inventarioRepository.findByInsumoId(dto.getInsumoId());

        if(inventarioExistente.isPresent()){
            throw new RuntimeException("El inventario para este insumo ya existe");
        }

        Inventario inventario = inventarioMapper.toEntity(dto, insumo);

        inventario = inventarioRepository.save(inventario);

        return inventarioMapper.toDTO(inventario);
    }

    @Override
    public InventarioResponseDTO actualizar(Long id, InventarioUpdateDTO dto) {

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if(dto.getStock() < 0){
            throw new RuntimeException("El stock no puede ser negativo");
        }

        inventario.setStock(dto.getStock());

        inventarioRepository.save(inventario);

        return inventarioMapper.toDTO(inventario);
    }

    @Override
    public List<InventarioResponseDTO> obtenerTodos() {

        return inventarioRepository.findAll()
                .stream()
                .map(inventarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void descontarInsumosProducto(Long productoId, Integer cantidadPedido) {

        List<ProductoInsumo> receta =
                productoInsumoRepository.findByProductoId(productoId);

        if (receta.isEmpty()) {
            throw new IllegalStateException("El producto no tiene receta configurada");
        }

        for (ProductoInsumo pi : receta) {

            Long insumoId = pi.getInsumo().getId();

            Inventario inventario = inventarioRepository.findByInsumoId(insumoId)
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

            int cantidadADescontar = pi.getCantidad() * cantidadPedido;

            if (inventario.getStock() < cantidadADescontar) {
                throw new IllegalArgumentException(
                        "Stock insuficiente de " + pi.getInsumo().getNombre()
                );
            }

            inventario.setStock(inventario.getStock() - cantidadADescontar);
            inventarioRepository.save(inventario);

        }
    }

    @Override
    public void eliminar(Long id) {

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        inventarioRepository.delete(inventario);

    }
}
