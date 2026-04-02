package com.marymar.app.business.Service.impl;

import com.marymar.app.business.DTO.InventarioCreateDTO;
import com.marymar.app.business.DTO.InventarioResponseDTO;
import com.marymar.app.business.DTO.InventarioUpdateDTO;
import com.marymar.app.business.Service.InventarioService;
import com.marymar.app.persistence.Entity.*;
import com.marymar.app.persistence.Mapper.InventarioMapper;
import com.marymar.app.persistence.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final InsumoRepository insumoRepository;
    private final InventarioMapper inventarioMapper;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoRepository productoRepository;
    private final ConsumoInventarioRepository consumoInventarioRepository;

    public InventarioServiceImpl(
            InventarioRepository inventarioRepository,
            InsumoRepository insumoRepository,
            InventarioMapper inventarioMapper, ProductoInsumoRepository productoInsumoRepository, ProductoRepository productoRepository, ConsumoInventarioRepository consumoInventarioRepository) {
        this.inventarioRepository = inventarioRepository;
        this.insumoRepository = insumoRepository;
        this.inventarioMapper = inventarioMapper;
        this.productoInsumoRepository = productoInsumoRepository;
        this.productoRepository = productoRepository;
        this.consumoInventarioRepository = consumoInventarioRepository;
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

    @Transactional
    @Override
    public InventarioResponseDTO actualizar(Long id, InventarioUpdateDTO dto) {

        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if(dto.getStock() < 0){
            throw new RuntimeException("El stock no puede ser negativo");
        }

        inventario.setStock(dto.getStock());

        inventarioRepository.save(inventario);

        try {
            actualizarDisponibilidadProductos(inventario.getInsumo().getId());
        } catch (Exception e) {
            e.printStackTrace();

            throw e;
        }
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

    @Override
    public void validarStockPedido(Pedido pedido) {

        for (DetallePedido d : pedido.getDetalles()) {

            List<ProductoInsumo> receta =
                    productoInsumoRepository.findByProductoId(d.getProducto().getId());

            for (ProductoInsumo pi : receta) {

                Long insumoId = pi.getInsumo().getId();
                int consumo = pi.getCantidad() * d.getCantidad();

                Inventario inventario = inventarioRepository.findByInsumoId(insumoId)
                        .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

                if (inventario.getStock() < consumo) {
                    throw new IllegalArgumentException(
                            "Stock insuficiente de " + pi.getInsumo().getNombre()
                    );
                }
            }
        }
    }

    @Override
    public void descontarStockPedido(Pedido pedido) {

        for (DetallePedido d : pedido.getDetalles()) {

            List<ProductoInsumo> receta =
                    productoInsumoRepository.findByProductoId(d.getProducto().getId());

            for (ProductoInsumo pi : receta) {

                Long insumoId = pi.getInsumo().getId();
                int consumo = pi.getCantidad() * d.getCantidad();

                Inventario inventario = inventarioRepository.findByInsumoId(insumoId)
                        .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

                inventario.setStock(inventario.getStock() - consumo);
                inventarioRepository.save(inventario);
                actualizarDisponibilidadProductos(insumoId);

                ConsumoInventario registro = new ConsumoInventario(
                        pedido.getId(),
                        insumoId,
                        consumo,
                        LocalDateTime.now()
                );

                consumoInventarioRepository.save(registro);
            }
        }
    }

    @Override
    public void validarStockProductoPedido(Long productoId, int cantidad) {

        List<ProductoInsumo> receta =
                productoInsumoRepository.findByProductoId(productoId);

        if (receta.isEmpty()) {
            throw new IllegalStateException("El producto no tiene receta configurada");
        }

        for (ProductoInsumo pi : receta) {

            Long insumoId = pi.getInsumo().getId();
            int consumo = pi.getCantidad() * cantidad;

            Inventario inventario = inventarioRepository.findByInsumoId(insumoId)
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

            if (inventario.getStock() < consumo) {
                throw new IllegalArgumentException(
                        "No hay suficiente stock de " + pi.getInsumo().getNombre()
                );
            }
        }
    }

    @Override
    public void actualizarDisponibilidadProductos(Long insumoId) {

        List<ProductoInsumo> relaciones =
                productoInsumoRepository.findByInsumoId(insumoId);

        for (ProductoInsumo pi : relaciones) {

            Long productoId = pi.getProducto().getId();

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            List<ProductoInsumo> receta =
                    productoInsumoRepository.findByProductoId(producto.getId());

            boolean disponible = true;

            for (ProductoInsumo r : receta) {

                Optional<Inventario> invOpt = inventarioRepository
                        .findByInsumoId(r.getInsumo().getId());

                if (invOpt.isEmpty() || invOpt.get().getStock() <= 0) {
                    disponible = false;
                    break;
                }
            }

            producto.setActivo(disponible);
            productoRepository.save(producto);
        }
    }
}
