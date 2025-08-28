package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @Column(name = "PRIMER_APELLIDO")
    private String primerApellido;

    @Column(name = "SEGUNDO_APELLIDO")
    private String segundoApellido;

    @Column(name = "DIRECCION")
    private String direccion;

    @Column(name = "TELEFONO", length = 10)
    private Long telefono;

    @Column(name = "USUARIO")
    private String nombreUsuario;

    @Column(name = "CONTRASENA")
    private String contrasena;

    @ManyToOne
    @JoinColumn(name = "TIPO_USUARIO", nullable = false)
    private TipoUsuario tipoUsuario;
    
    @ManyToOne
    @JoinColumn(name = "PERFILES_ID_PERFIL", nullable = false)
    private Perfil perfil;

}
