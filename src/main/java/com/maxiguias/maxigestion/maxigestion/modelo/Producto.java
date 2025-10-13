package com.maxiguias.maxigestion.maxigestion.modelo;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonManagedReference
    private List<Terminado> terminados = new ArrayList<>();

}
