/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.maxiguias.maxigestion.maxigestion.modelo;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class FormularioXPerfilId implements Serializable{
    @Column(name = "PERFILES_ID_PERFIL")
    private Long perfilId;

    @Column(name = "FORMULARIOS_ID_FORMULARIO")
    private Long formularioId;
}
