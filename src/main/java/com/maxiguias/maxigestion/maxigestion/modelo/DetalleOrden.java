package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_ordenes")
@Data
@IdClass(DetalleOrdenId.class)
public class DetalleOrden implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_FACTURA")
    private Orden orden;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_TERMINADO")
    private Terminado terminado;

    @Column(name = "DESCRIPCION_PRODUCTO")
    private String descripcion;

    @Column(name = "CANTIDAD_PRODUCTO")
    private Integer cantidad;

    @Column(name = "VALOR_PRODUCTO")
    private BigDecimal valor;
}