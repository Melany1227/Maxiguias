package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "FORMULARIOS")
@Data
public class Formulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_FORMULARIO")
    private Long id;

    @Column(name = "NOMBRE_FORMULARIO", nullable = false, length = 50)
    private String nombreFormulario;

    @Column(name = "URL", nullable = false, length = 255)
    private String url;

    @Column(name = "PADRE")
    private Integer padre;
}

