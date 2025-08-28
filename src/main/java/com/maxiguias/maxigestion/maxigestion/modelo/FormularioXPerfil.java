package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "FORMULARIOS_X_PERFILES")
@Data
public class FormularioXPerfil {

    @EmbeddedId
    private FormularioXPerfilId id;

    @Column(name = "CREAR", nullable = false)
    private char crear;

    @Column(name = "EDITAR", nullable = false)
    private char editar;

    @Column(name = "VISUALIZAR", nullable = false)
    private char visualizar;

    @Column(name = "ELIMINAR", nullable = false)
    private char eliminar;

    @ManyToOne
    @MapsId("perfilId")
    @JoinColumn(name = "PERFILES_ID_PERFIL")
    private Perfil perfil;

    @ManyToOne
    @MapsId("formularioId")
    @JoinColumn(name = "FORMULARIOS_ID_FORMULARIO")
    private Formulario formulario;

}
