package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
}
