package com.marymar.app.controller;

import com.marymar.app.business.DTO.PedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;
import com.marymar.app.business.Service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PreAuthorize("hasAnyRole('CLIENTE','MESERO')")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@RequestBody PedidoCreateDTO dto){
        return ResponseEntity.ok(pedidoService.crearPedido(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','MESERO')")
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtener(@PathVariable Long id){
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','MESERO')")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPorCliente(@PathVariable Long clienteId){
        return ResponseEntity.ok(pedidoService.obtenerPorCliente(clienteId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','MESERO')")
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> obtenerTodos(){
        return ResponseEntity.ok(pedidoService.obtenerTodos());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado){

        return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
    }
}