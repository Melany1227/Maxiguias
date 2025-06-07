package com.maxiguias.maxigestion.maxigestion.repositorio;

import com.maxiguias.maxigestion.maxigestion.modelo.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, String> {

    // Consulta nativa para obtener datos del objeto Oracle - convertir teléfono a string
    @Query(value = """
        SELECT
            e.nit_empresa,
            e.fecha_creacion_empresa,
            e.info_persona.nombre as nombre,
            e.info_persona.direccion as direccion,
            TO_CHAR(e.info_persona.telefono) as telefono
        FROM empresas e
        ORDER BY e.nit_empresa
        """, nativeQuery = true)
    List<Object[]> findAllEmpresasWithObjectData();
}
