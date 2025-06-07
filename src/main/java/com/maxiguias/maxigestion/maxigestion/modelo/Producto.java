package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

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

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Terminados> terminados;
}

