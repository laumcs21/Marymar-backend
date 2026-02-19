package com.marymar.app.business.DTO;

import java.time.LocalDateTime;

public class ProductoImagenResponseDTO {

    private Long id;
    private String url;
    private Integer orden;
    private Boolean principal;
    private LocalDateTime createdAt;

    public ProductoImagenResponseDTO() {}

    public ProductoImagenResponseDTO(Long id,
                                     String url,
                                     Integer orden,
                                     Boolean principal,
                                     LocalDateTime createdAt) {
        this.id = id;
        this.url = url;
        this.orden = orden;
        this.principal = principal;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {   // opcional, pero lo dejo por consistencia
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

