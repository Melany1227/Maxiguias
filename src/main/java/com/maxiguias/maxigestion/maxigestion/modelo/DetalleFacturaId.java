package com.maxiguias.maxigestion.maxigestion.modelo;
import lombok.Data;
import java.io.Serializable;
@Data
public class DetalleFacturaId implements Serializable {
    private Long factura;
    private Long producto;
}

