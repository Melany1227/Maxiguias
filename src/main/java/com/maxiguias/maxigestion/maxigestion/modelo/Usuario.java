package com.maxiguias.maxigestion.maxigestion.modelo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

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

    @Column(name = "FECHA_REGISTRO", insertable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaRegistro;

    @ManyToOne
    @JoinColumn(name = "TIPO_USUARIO", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TipoUsuario tipoUsuario;
    
    @ManyToOne
    @JoinColumn(name = "PERFILES_ID_PERFIL", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Perfil perfil;

    @ManyToOne
    @JoinColumn(name = "id_ciudad", nullable = false)
    @JsonIgnoreProperties({"departamento"})
    private Ciudad ciudad;

}
