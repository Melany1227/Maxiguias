package com.maxiguias.maxigestion.maxigestion.modelo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "TERMINADOS")
@Data
public class Terminado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TERMINADO")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_PRODUCTO", nullable = false)
    private Producto producto;

    @Column(name = "MEDIDA_TERMINADO_PRODUCTO", nullable = false, precision = 4, scale = 2)
    private BigDecimal medidaTerminadoProducto;

    @Column(name = "PRECIO_PUBLICO", nullable = false)
    private Integer precioPublico;

    @Column(name = "PRECIO_X_MAYOR", nullable = false)
    private Integer precioPorMayor;

    @Column(name = "PRECIO_X_ENCARGO", nullable = false)
    private Integer precioPorEncargo;

}
