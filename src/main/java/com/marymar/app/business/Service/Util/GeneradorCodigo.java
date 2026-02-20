package com.marymar.app.business.Service.Util;

public interface GeneradorCodigo {
    String generarCodigo(String email);

    void validarCodigo(String email, String code);
}