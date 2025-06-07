package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "USUARIOS")
@Data
public class Usuario {

    @Id
    @Column(name = "DOCUMENTO")
    private Long documento;

    @Column(name = "NOMBRE", length = 100, nullable = false)
    private String nombre;

    @Column(name = "PRIMER_APELLIDO", length = 50, nullable = false)
    private String primerApellido;

    @Column(name = "SEGUNDO_APELLIDO", length = 50)
    private String segundoApellido;

    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "TELEFONO", length = 10)
    private Long telefono;

    @Column(name = "USUARIO", length = 30, nullable = false, unique = true)
    private String usuario;

    @Column(name = "CONTRASENA", length = 30, nullable = false)
    private String contrasena;

    @ManyToOne
    @JoinColumn(name = "TIPO_USUARIO", nullable = false)
    private TipoUsuario tipoUsuario;

}
