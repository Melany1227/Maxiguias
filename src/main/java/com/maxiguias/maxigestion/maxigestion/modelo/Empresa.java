package com.maxiguias.maxigestion.maxigestion.modelo;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;

import java.util.Date;
@Entity
@Table(name = "empresas")
@Data
public class Empresa {

    @Id
    @Column(name = "nit_empresa")
    private String nitEmpresa;

    @Column(name = "nombre_empresa")
    private String nombreEmpresa;

    @Column(name = "fecha_creacion_empresa")
    private Date fechaCreacionEmpresa;

}
