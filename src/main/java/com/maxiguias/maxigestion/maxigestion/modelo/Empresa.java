package com.maxiguias.maxigestion.maxigestion.modelo;

import java.util.Date;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Entity
@Table(name = "EMPRESAS")
@Data
public class Empresa {

    @Id
    @Column(name = "NIT_EMPRESA")
    private String nitEmpresa;

    @Column(name = "FECHA_CREACION_EMPRESA")
    @Temporal(TemporalType.DATE)
    private Date fechaCreacionEmpresa;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "nombre", column = @Column(name = "INFORMACION_PERSONAL.NOMBRE")),
        @AttributeOverride(name = "direccion", column = @Column(name = "INFORMACION_PERSONAL.DIRECCION")),
        @AttributeOverride(name = "telefono", column = @Column(name = "INFORMACION_PERSONAL.TELEFONO"))
    })
    private PersonaInfo infoPersona;

    public Empresa() {}


    // Constructor con parámetros
    public Empresa(String nitEmpresa, Date fechaCreacionEmpresa, PersonaInfo infoPersona) {
        this.nitEmpresa = nitEmpresa;
        this.fechaCreacionEmpresa = fechaCreacionEmpresa;
        this.infoPersona = infoPersona;
    }

}
