package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "PRODUCTOS")
@Data
public class Producto {
    @Id
    @Column(name = "ID_PRODUCTO")
    private Long id;

    @Column(name = "NOMBRE_GUIA")
    private String nombre;

    @Column(name = "IMAGEN_PRODUCTO")
    private String imagen;

    @Column(name = "CANTIDAD_DISPONIBLE")
    private Integer cantidadDisponible;
}

