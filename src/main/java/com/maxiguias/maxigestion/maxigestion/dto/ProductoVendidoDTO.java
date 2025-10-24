package com.maxiguias.maxigestion.maxigestion.dto;

import java.math.BigDecimal;

public class ProductoVendidoDTO {
    private String nombreProducto;
    private BigDecimal medida;
    private Long cantidadVendida;
    
    public ProductoVendidoDTO(String nombreProducto, BigDecimal medida, Long cantidadVendida) {
        this.nombreProducto = nombreProducto;
        this.medida = medida;
        this.cantidadVendida = cantidadVendida;
    }
    
    // Getters y setters
    public String getNombreProducto() {
        return nombreProducto;
    }
    
    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
    
    public BigDecimal getMedida() {
        return medida;
    }
    
    public void setMedida(BigDecimal medida) {
        this.medida = medida;
    }
    
    public Long getCantidadVendida() {
        return cantidadVendida;
    }
    
    public void setCantidadVendida(Long cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }
    
    public String getProductoConMedida() {
        return nombreProducto + " (" + medida + ")";
    }
}