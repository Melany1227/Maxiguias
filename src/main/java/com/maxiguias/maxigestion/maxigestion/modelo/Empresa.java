package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "EMPRESAS")
public class Empresa {

    @Id
    @Column(name = "NIT_EMPRESA")
    private String nitEmpresa;

    @Column(name = "FECHA_CREACION_EMPRESA")
    @Temporal(TemporalType.DATE)
    private Date fechaCreacionEmpresa;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "nombre", column = @Column(name = "INFO_PERSONA.NOMBRE")),
        @AttributeOverride(name = "direccion", column = @Column(name = "INFO_PERSONA.DIRECCION")),
        @AttributeOverride(name = "telefono", column = @Column(name = "INFO_PERSONA.TELEFONO"))
    })
    private PersonaInfo infoPersona;

    // Constructor vacío
    public Empresa() {}

    // Constructor con parámetros
    public Empresa(String nitEmpresa, Date fechaCreacionEmpresa, PersonaInfo infoPersona) {
        this.nitEmpresa = nitEmpresa;
        this.fechaCreacionEmpresa = fechaCreacionEmpresa;
        this.infoPersona = infoPersona;
    }

    // Getters y Setters
    public String getNitEmpresa() {
        return nitEmpresa;
    }

    public void setNitEmpresa(String nitEmpresa) {
        this.nitEmpresa = nitEmpresa;
    }

    public Date getFechaCreacionEmpresa() {
        return fechaCreacionEmpresa;
    }

    public void setFechaCreacionEmpresa(Date fechaCreacionEmpresa) {
        this.fechaCreacionEmpresa = fechaCreacionEmpresa;
    }

    public PersonaInfo getInfoPersona() {
        return infoPersona;
    }

    public void setInfoPersona(PersonaInfo infoPersona) {
        this.infoPersona = infoPersona;
    }

    // Métodos de conveniencia para acceder a los datos del objeto
    public String getNombre() {
        return infoPersona != null ? infoPersona.getNombre() : null;
    }

    public String getDireccion() {
        return infoPersona != null ? infoPersona.getDireccion() : null;
    }

    public String getTelefono() {
        return infoPersona != null ? infoPersona.getTelefono() : null;
    }
}
