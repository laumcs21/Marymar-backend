package com.marymar.app.controller;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.marymar.app.business.DTO.DetallePedidoResponseDTO;
import com.marymar.app.business.DTO.PedidoCreateDTO;
import com.marymar.app.business.DTO.PedidoResponseDTO;
import com.marymar.app.business.Service.PedidoService;
import com.marymar.app.persistence.Entity.DetallePedido;
import com.marymar.app.persistence.Entity.Pedido;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
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
    public ResponseEntity<PedidoResponseDTO> crearPedido(@RequestBody PedidoCreateDTO dto) {
        return ResponseEntity.ok(pedidoService.crearPedido(dto));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','MESERO')")
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','MESERO')")
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.obtenerPorCliente(clienteId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','CLIENTE','MESERO')")
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(pedidoService.obtenerTodos());
    }

    @PreAuthorize("hasAnyRole('ADMIN','MESERO')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {

        return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
    }

    @PreAuthorize("hasRole('MESERO')")
    @GetMapping("/mesa/{mesaId}")
    public ResponseEntity<PedidoResponseDTO> obtenerPorMesa(@PathVariable Long mesaId) {
        return ResponseEntity.ok(pedidoService.obtenerPedidoPorMesa(mesaId));
    }

    // =========================
    // AGREGAR PRODUCTO
    // =========================
    @PreAuthorize("hasRole('MESERO')")
    @PostMapping("/{pedidoId}/agregar-producto")
    public ResponseEntity<PedidoResponseDTO> agregarProducto(
            @PathVariable Long pedidoId,
            @RequestParam Long productoId,
            @RequestParam int cantidad) {

        return ResponseEntity.ok(
                pedidoService.agregarProducto(pedidoId, productoId, cantidad)
        );
    }

    // =========================
    // DISMINUIR PRODUCTO
    // =========================
    @PreAuthorize("hasRole('MESERO')")
    @PutMapping("/{pedidoId}/disminuir-producto")
    public ResponseEntity<PedidoResponseDTO> disminuirProducto(
            @PathVariable Long pedidoId,
            @RequestParam Long productoId) {

        return ResponseEntity.ok(
                pedidoService.disminuirProducto(pedidoId, productoId)
        );
    }

    // =========================
    // ELIMINAR DETALLE
    // =========================
    @PreAuthorize("hasRole('MESERO')")
    @DeleteMapping("/{pedidoId}/detalle/{detalleId}")
    public ResponseEntity<PedidoResponseDTO> eliminarDetalle(
            @PathVariable Long pedidoId,
            @PathVariable Long detalleId) {

        return ResponseEntity.ok(
                pedidoService.eliminarDetalle(pedidoId, detalleId)
        );
    }

    @PreAuthorize("hasRole('MESERO')")
    @PostMapping("/mesa/{mesaId}/abrir")
    public ResponseEntity<PedidoResponseDTO> abrirPedido(
            @PathVariable Long mesaId,
            @RequestParam Long meseroId) {

        return ResponseEntity.ok(
                pedidoService.obtenerOCrearPedidoPorMesa(mesaId, meseroId)
        );
    }

    @GetMapping("/{id}/factura")
    public ResponseEntity<byte[]> generarFactura(@PathVariable Long id) throws Exception {

        PedidoResponseDTO pedido = pedidoService.obtenerPorId(id);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("FACTURA - MAR Y MAR"));
        document.add(new Paragraph("Pedido #" + pedido.getId()));
        document.add(new Paragraph("Fecha: " + pedido.getFecha()));
        if (pedido.getClienteNombre() == null){
            document.add(new Paragraph("Cliente: No especificado"));
        }else{
            document.add(new Paragraph("Cliente: " + pedido.getClienteNombre()));
        }

        document.add(new Paragraph(" "));

        for (DetallePedidoResponseDTO d : pedido.getDetalles()) {
            document.add(new Paragraph(
                    d.getProductoNombre() + " x" + d.getCantidad() +
                            " - $" + d.getPrecioUnitario()
            ));
        }

        document.add(new Paragraph(" "));
        document.add(new Paragraph("TOTAL: $" + pedido.getTotal()));

        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=factura.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }

    @GetMapping("/{id}/comanda")
    public ResponseEntity<byte[]> generarComanda(@PathVariable Long id) throws Exception {

        PedidoResponseDTO pedido = pedidoService.obtenerPorId(id);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("COMANDA COCINA"));
        document.add(new Paragraph("Mesa: " + pedido.getNumeroMesa()));
        document.add(new Paragraph("Mesero: " + pedido.getMeseroNombre()));

        document.add(new Paragraph("-------------------"));

        for (DetallePedidoResponseDTO d : pedido.getDetalles()) {
            document.add(new Paragraph(
                    d.getCantidad() + " x " + d.getProductoNombre()
            ));
        }

        document.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=comanda.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }
}