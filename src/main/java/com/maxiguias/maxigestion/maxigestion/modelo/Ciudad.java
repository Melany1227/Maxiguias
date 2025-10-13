package com.maxiguias.maxigestion.maxigestion.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "CIUDADES")
@Data
public class Ciudad {
    @Id
    @Column(name = "ID_CIUDAD")
    private Integer id;

    @Column(name = "NOMBRE_CIUDAD")
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departamento")
    @JsonIgnoreProperties({"ciudades"})
    private Departamento departamento;
}
