package com.maxiguias.maxigestion.maxigestion.dto;

import java.math.BigDecimal;

public class ProductoCatalogoDTO {

    private Long id;
    private String nombre;
    private Integer cantidad;
    private BigDecimal precio;
    private String imagen;

    public ProductoCatalogoDTO() {
    }

    public ProductoCatalogoDTO(Long id, String nombre, Integer cantidad, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
