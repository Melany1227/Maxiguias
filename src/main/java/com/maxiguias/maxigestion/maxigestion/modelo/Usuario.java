package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

    @Column(name = "PRIMER_APELLIDO", length = 50, nullable = false)
    private String primerApellido;

    @Column(name = "SEGUNDO_APELLIDO", length = 50)
    private String segundoApellido;

    @Column(name = "USUARIO", length = 30, nullable = false, unique = true)
    private String usuario;

    @Column(name = "CONTRASENA", length = 30, nullable = false)
    private String contrasena;

    @ManyToOne
    @JoinColumn(name = "TIPO_USUARIO", nullable = false)
    private TipoUsuario tipoUsuario;

    @Column(name = "PERFILES_ID_PERFIL")
    private Integer perfilesIdPerfil;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "nombre", column = @Column(name = "INFORMACION_PERSONAL.NOMBRE")),
        @AttributeOverride(name = "direccion", column = @Column(name = "INFORMACION_PERSONAL.DIRECCION")),
        @AttributeOverride(name = "telefono", column = @Column(name = "INFORMACION_PERSONAL.TELEFONO"))
    })
    private PersonaInfo infoPersona;

     // Constructor vacío
    public Usuario() {}

    // Constructor con parámetros
    public Usuario(Long documento, String primerApellido, String segundoApellido,
                   String usuario, String contrasena, TipoUsuario tipoUsuario,
                   Integer perfilesIdPerfil, PersonaInfo infoPersona) {
        this.documento = documento;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.tipoUsuario = tipoUsuario;
        this.perfilesIdPerfil = perfilesIdPerfil;
        this.infoPersona = infoPersona;
    }

    // Métodos de conveniencia para acceder a los datos del objeto persona_info
    public String getNombre() {
        return infoPersona != null ? infoPersona.getNombre() : null;
    }

   

    public String getDireccion() {
        return infoPersona != null ? infoPersona.getDireccion() : null;
    }

   

    public String getTelefono() {
        return infoPersona != null ? infoPersona.getTelefono() : null;
    }

  
    // Método para obtener nombre completo
    public String getNombreCompleto() {
        StringBuilder nombreCompleto = new StringBuilder();

        if (getNombre() != null) {
            nombreCompleto.append(getNombre());
        }

        if (primerApellido != null) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(primerApellido);
        }

        if (segundoApellido != null) {
            if (nombreCompleto.length() > 0) nombreCompleto.append(" ");
            nombreCompleto.append(segundoApellido);
        }

        return nombreCompleto.toString();
    }

}
