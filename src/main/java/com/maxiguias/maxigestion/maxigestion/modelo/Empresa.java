package com.maxiguias.maxigestion.maxigestion.modelo;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;

import java.util.Date;
@Entity
@Table(name = "EMPRESAS")
@Data
public class Empresa {

    @Id
    @Column(name = "NIT_EMPRESA")
    private String nitEmpresa;

    @Column(name = "NOMBRE_EMPRESA")
    private String nombreEmpresa;

    @Column(name = "FECHA_CREACION_EMPRESA")
    private Date fechaCreacionEmpresa;

}
