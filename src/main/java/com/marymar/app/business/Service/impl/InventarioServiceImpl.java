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
    private final LoteInsumoRepository loteInsumoRepository;
    private final InventarioMapper inventarioMapper;
    private final ProductoInsumoRepository productoInsumoRepository;
    private final ProductoRepository productoRepository;
    private final ConsumoInventarioRepository consumoInventarioRepository;

    public InventarioServiceImpl(
            InventarioRepository inventarioRepository,
            InsumoRepository insumoRepository, LoteInsumoRepository loteInsumoRepository,
            InventarioMapper inventarioMapper, ProductoInsumoRepository productoInsumoRepository, ProductoRepository productoRepository, ConsumoInventarioRepository consumoInventarioRepository) {
        this.inventarioRepository = inventarioRepository;
        this.insumoRepository = insumoRepository;
        this.loteInsumoRepository = loteInsumoRepository;
        this.inventarioMapper = inventarioMapper;
        this.productoInsumoRepository = productoInsumoRepository;
        this.productoRepository = productoRepository;
        this.consumoInventarioRepository = consumoInventarioRepository;
    }

    @Transactional
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

        int stockAnterior = inventario.getStock();
        int nuevoStock = dto.getStock();

        inventario.setStock(nuevoStock);
        inventarioRepository.save(inventario);

        // 🔥 AQUÍ ESTÁ LO QUE TE FALTABA
        if (nuevoStock > stockAnterior) {
            int cantidadEntrada = nuevoStock - stockAnterior;

            registrarEntradaLote(
                    inventario.getInsumo().getId(),
                    cantidadEntrada,
                    LocalDateTime.now().plusDays(7) // o lo que uses
            );
        }

        actualizarDisponibilidadProductos(inventario.getInsumo().getId());

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

            int stockCocina = obtenerStockCocina(insumoId);

            if (stockCocina < cantidadADescontar) {
                throw new IllegalArgumentException(
                        "Stock insuficiente en cocina de " + pi.getInsumo().getNombre()
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

                int stockCocina = obtenerStockCocina(insumoId);

                if (stockCocina < consumo) {
                    throw new IllegalArgumentException(
                            "Stock insuficiente en cocina de " + pi.getInsumo().getNombre()
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

                int stockCocina = obtenerStockCocina(insumoId);

                if (stockCocina < consumo) {
                    throw new IllegalArgumentException(
                            "No hay suficiente stock en cocina de " + pi.getInsumo().getNombre()
                    );
                }

                descontarDeLotes(insumoId, consumo);

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

            int stockCocina = obtenerStockCocina(insumoId);

            if (stockCocina < consumo) {
                throw new IllegalArgumentException(
                        "Stock insuficiente en cocina de " + pi.getInsumo().getNombre()
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

    @Override
    public void registrarEntradaLote(Long insumoId, int cantidad, LocalDateTime fechaVencimiento) {

        LoteInsumo lote = new LoteInsumo();

        lote.setInsumo(insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado")));

        lote.setCantidadInicial(cantidad);
        lote.setCantidadDisponible(cantidad);

        lote.setFechaIngreso(LocalDateTime.now());
        lote.setFechaVencimiento(fechaVencimiento);

        lote.setUbicacion(UbicacionInventario.BODEGA);
        lote.setEstado(EstadoLote.ACTIVO);

        loteInsumoRepository.save(lote);
    }

    @Override
    public void descontarDeLotes(Long insumoId, int cantidadNecesaria) {

        List<LoteInsumo> lotes = loteInsumoRepository
                .findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                        insumoId,
                        UbicacionInventario.COCINA,
                        EstadoLote.ACTIVO
                );

        int restante = cantidadNecesaria;

        for (LoteInsumo lote : lotes) {

            if (restante <= 0) break;

            int disponible = lote.getCantidadDisponible();

            if (disponible <= 0) continue;

            if (disponible >= restante) {
                lote.setCantidadDisponible(disponible - restante);
                restante = 0;
            } else {
                lote.setCantidadDisponible(0);
                restante -= disponible;
            }

            // actualizar estado
            if (lote.getCantidadDisponible() == 0) {
                lote.setEstado(EstadoLote.AGOTADO);
            }

            loteInsumoRepository.save(lote);
        }

        if (restante > 0) {
            throw new IllegalArgumentException("Stock insuficiente (lotes)");
        }
    }

    @Transactional
    @Override
    public void ingresarStock(Long insumoId, int cantidad, LocalDateTime fechaVencimiento) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad inválida");
        }

        Inventario inventario = inventarioRepository.findByInsumoId(insumoId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        registrarEntradaLote(insumoId, cantidad, fechaVencimiento);

        inventario.setStock(inventario.getStock() + cantidad);
        inventarioRepository.save(inventario);
    }

    @Transactional
    @Override
    public void surtirCocina(Long insumoId, int cantidad) {

        List<LoteInsumo> lotesBodega = loteInsumoRepository
                .findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                        insumoId,
                        UbicacionInventario.BODEGA,
                        EstadoLote.ACTIVO
                );

        int restante = cantidad;

        for (LoteInsumo lote : lotesBodega) {

            if (restante <= 0) break;

            int disponible = lote.getCantidadDisponible();

            if (disponible <= 0) continue;

            int aMover = Math.min(disponible, restante);

            // 🔻 quitar de bodega
            lote.setCantidadDisponible(disponible - aMover);

            if (lote.getCantidadDisponible() == 0) {
                lote.setEstado(EstadoLote.AGOTADO);
            }

            loteInsumoRepository.save(lote);

            LoteInsumo loteCocina = new LoteInsumo();
            loteCocina.setInsumo(lote.getInsumo());
            loteCocina.setCantidadInicial(aMover);
            loteCocina.setCantidadDisponible(aMover);
            loteCocina.setFechaIngreso(lote.getFechaIngreso());
            loteCocina.setFechaVencimiento(lote.getFechaVencimiento());
            loteCocina.setUbicacion(UbicacionInventario.COCINA);
            loteCocina.setEstado(EstadoLote.ACTIVO);

            loteInsumoRepository.save(loteCocina);

            restante -= aMover;
        }

        if (restante > 0) {
            throw new IllegalArgumentException("No hay suficiente en bodega para surtir cocina");
        }
    }

    @Override
    public int obtenerStockCocina(Long insumoId) {
        List<LoteInsumo> lotesCocina = loteInsumoRepository
                .findByInsumoIdAndUbicacionAndEstadoOrderByFechaVencimientoAscFechaIngresoAsc(
                        insumoId,
                        UbicacionInventario.COCINA,
                        EstadoLote.ACTIVO
                );

        return lotesCocina.stream()
                .mapToInt(LoteInsumo::getCantidadDisponible)
                .sum();
    }
}
