package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "perfiles")
@Data
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PERFIL")
    private Long id;

    @Column(name = "NOMBRE_PERFIL", nullable = false, length = 50)
    private String nombrePerfil;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ROLES_ID_ROL", nullable = false)
    private Rol rol;
}

