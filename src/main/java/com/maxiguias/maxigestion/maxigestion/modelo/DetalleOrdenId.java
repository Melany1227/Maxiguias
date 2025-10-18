package com.maxiguias.maxigestion.maxigestion.modelo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode
public class DetalleOrdenId implements Serializable {
    
    private Long orden;
    private Long terminado;
    
    public DetalleOrdenId() {}
    
    public DetalleOrdenId(Long orden, Long terminado) {
        this.orden = orden;
        this.terminado = terminado;
    }
}