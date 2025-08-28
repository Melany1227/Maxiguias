package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "TIPO_USUARIO")
@Data
public class TipoUsuario {

    @Id
    @Column(name = "ID_TIPO_USUARIO")
    private Integer id;

    @Column(name = "NOMBRE_TIPO_USUARIO", length = 30)
    private String nombre;

}
