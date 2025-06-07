package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.maxiguias.maxigestion.maxigestion.modelo.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    @Query(value = """
        SELECT
            e.nit_empresa,
            e.fecha_creacion_empresa,
            e.informacion_personal.nombre as nombre,
            e.informacion_personal.direccion as direccion,
            TO_CHAR(e.informacion_personal.telefono) as telefono
        FROM empresas e
        ORDER BY e.nit_empresa
        """, nativeQuery = true)
    List<Object[]> findAllEmpresasWithObjectData();
}
