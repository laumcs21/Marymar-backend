package com.marymar.app.TestSupport;

import com.marymar.app.business.DTO.*;
import com.marymar.app.persistence.Entity.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TestDataFactory {

    private TestDataFactory() {}

    public static void setField(Object target, String fieldName, Object value) {
        try {
            Class<?> type = target.getClass();
            while (type != null) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException ignored) {
                    type = type.getSuperclass();
                }
            }
            throw new IllegalArgumentException("Campo no encontrado: " + fieldName);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Categoria categoria(Long id, String nombre) {
        Categoria categoria = new Categoria(nombre);
        setField(categoria, "id", id);
        return categoria;
    }

    public static Persona persona(Long id, String nombre, String email, Rol rol) {
        Persona persona = Persona.builder()
                .numeroIdentificacion("ID-" + id)
                .nombre(nombre)
                .email(email)
                .contrasena("hash")
                .telefono("3000000000")
                .fechaNacimiento(LocalDate.of(1998, 1, 1))
                .rol(rol)
                .activo(true)
                .build();
        setField(persona, "id", id);
        persona.setAceptoHabeasData(true);
        persona.setFechaAceptacion(LocalDateTime.now());
        if (rol == Rol.CLIENTE) {
            persona.setDireccionEnvio("Calle 123");
        }
        if (rol == Rol.MESERO) {
            persona.setSalario(1800000d);
        }
        return persona;
    }

    public static PersonaCreateDTO personaCreateDTO(String rol) {
        PersonaCreateDTO dto = new PersonaCreateDTO();
        dto.setNumeroIdentificacion("123456789");
        dto.setNombre("Laura");
        dto.setEmail("laura@test.com");
        dto.setContrasena("Abc123$");
        dto.setTelefono("3000000000");
        dto.setFechaNacimiento(LocalDate.of(1998, 1, 1));
        dto.setRol(rol);
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            dto.setDireccionEnvio("Calle 123");
        }
        if ("MESERO".equalsIgnoreCase(rol)) {
            dto.setSalario(1800000d);
        }
        return dto;
    }

    public static Producto producto(Long id, String nombre, BigDecimal precio, Categoria categoria) {
        Producto producto = new Producto(nombre, precio, categoria, "Descripción " + nombre);
        setField(producto, "id", id);
        producto.setActivo(true);
        producto.setImagenes(new ArrayList<>());
        return producto;
    }

    public static ProductoResponseDTO productoResponseDTO(Long id, String nombre, BigDecimal precio, Long categoriaId, String categoriaNombre) {
        return new ProductoResponseDTO(id, nombre, precio, "Descripción " + nombre, categoriaId, categoriaNombre, true, List.of(), null);
    }

    public static Insumo insumo(Long id, String nombre, String unidad) {
        Insumo insumo = new Insumo(nombre, unidad);
        setField(insumo, "id", id);
        return insumo;
    }

    public static Inventario inventario(Long id, Insumo insumo, Integer stock) {
        Inventario inventario = new Inventario(insumo, stock);
        setField(inventario, "id", id);
        return inventario;
    }

    public static Mesa mesa(Long id, Integer numero, Integer capacidad) {
        Mesa mesa = new Mesa();
        setField(mesa, "id", id);
        mesa.setNumero(numero);
        mesa.setCapacidad(capacidad);
        mesa.setEstado(EstadoMesa.DISPONIBLE);
        mesa.setActiva(true);
        return mesa;
    }

    public static DetallePedido detalle(Long id, Producto producto, Integer cantidad) {
        DetallePedido detalle = new DetallePedido(producto, cantidad);
        detalle.setId(id);
        return detalle;
    }

    public static Pedido pedidoMesa(Long id, Mesa mesa, Persona mesero, Producto producto, int cantidad) {
        Pedido pedido = new Pedido(mesa, mesero);
        setField(pedido, "id", id);
        if (producto != null && cantidad > 0) {
            pedido.agregarDetalle(new DetallePedido(producto, cantidad));
        }
        pedido.calcularTotal();
        return pedido;
    }

    public static Pedido pedidoDomicilio(Long id, Persona cliente, Persona mesero, Producto producto, int cantidad) {
        Pedido pedido = new Pedido(cliente, mesero);
        setField(pedido, "id", id);
        if (producto != null && cantidad > 0) {
            pedido.agregarDetalle(new DetallePedido(producto, cantidad));
        }
        pedido.calcularTotal();
        return pedido;
    }

    public static Pago pago(Long id, Pedido pedido, MetodoPago metodo, BigDecimal monto) {
        Pago pago = new Pago(pedido, metodo, monto);
        setField(pago, "id", id);
        return pago;
    }

    public static ProductoInsumo productoInsumo(Long id, Producto producto, Insumo insumo, Integer cantidad) {
        ProductoInsumo pi = new ProductoInsumo(producto, insumo, cantidad);
        setField(pi, "id", id);
        return pi;
    }

    public static Auditoria auditoria(Long id, String usuario, String accion, String entidad) {
        Auditoria auditoria = new Auditoria();
        auditoria.setId(id);
        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setEntidadId(1L);
        auditoria.setDetalle("detalle");
        auditoria.setFecha(LocalDateTime.now());
        return auditoria;
    }

    public static RegisterRequestDTO registerRequest() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setNumeroIdentificacion("123456789");
        request.setNombre("Laura");
        request.setEmail("laura@test.com");
        request.setContrasena("Admin123$");
        request.setTelefono("3000000000");
        request.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        request.setAceptaHabeasData(true);
        request.setCaptchaToken("captcha-ok");
        return request;
    }

    public static LoginRequestDTO loginRequest() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("laura@test.com");
        dto.setContrasena("Admin123$");
        dto.setCaptchaToken("captcha-ok");
        return dto;
    }

    public static PedidoCreateDTO pedidoCreateMesa(Long mesaId, Long meseroId, Long productoId, int cantidad) {
        PedidoCreateDTO dto = new PedidoCreateDTO();
        dto.setMesaId(mesaId);
        dto.setMeseroId(meseroId);
        dto.setDetalles(List.of(new DetallePedidoCreateDTO(productoId, cantidad)));
        setField(dto, "tipo", "MESA");
        return dto;
    }

    public static PedidoCreateDTO pedidoCreateDomicilio(Long clienteId, Long meseroId, Long productoId, int cantidad) {
        PedidoCreateDTO dto = new PedidoCreateDTO();
        dto.setClienteId(clienteId);
        dto.setMeseroId(meseroId);
        dto.setDetalles(List.of(new DetallePedidoCreateDTO(productoId, cantidad)));
        setField(dto, "tipo", "DOMICILIO");
        return dto;
    }
}
