package com.maxiguias.maxigestion.maxigestion.modelo;

import java.io.Serializable;

import lombok.Data;
@Data
public class DetalleFacturaId implements Serializable {
    private Long factura;
    private Long producto;
}

