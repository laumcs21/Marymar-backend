package com.marymar.app.business.DTO;

public class NotificacionDTO {

    private String mensaje;
    private String tipo;
    private String insumoNombre;

    public NotificacionDTO() {
    }

    public NotificacionDTO(String mensaje, String tipo, String insumoNombre) {
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.insumoNombre = insumoNombre;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getInsumoNombre() {
        return insumoNombre;
    }

    public void setInsumoNombre(String insumoNombre) {
        this.insumoNombre = insumoNombre;
    }
}
