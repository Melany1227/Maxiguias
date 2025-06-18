package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "DETALLE_FACTURAS")
@Data
public class DetalleFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinColumn(name = "ID_DETALLE_FACTURA")
    private Long id_detalle_factura;

    @ManyToOne
    @JoinColumn(name = "ID_FACTURA")
    private Factura factura;

    @ManyToOne
    @JoinColumn(name = "ID_PRODUCTO")
    private Producto producto;

    @Column(name = "DESCRIPCION_PRODUCTO")
    private String descripcion;

    @Column(name = "CANTIDAD_PRODUCTO")
    private Integer cantidad;

    @Column(name = "VALOR_PRODUCTO")
    private BigDecimal valor;
}

