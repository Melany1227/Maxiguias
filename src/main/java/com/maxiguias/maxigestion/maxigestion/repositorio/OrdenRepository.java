package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maxiguias.maxigestion.maxigestion.modelo.Orden;

public interface OrdenRepository extends JpaRepository<Orden, Long> {

    // Método para contar todas las órdenes
    Long countBy();

    // Métodos para reportes con filtro por mes
    @Query("SELECT COUNT(o) FROM Orden o WHERE MONTH(o.fechaOrden) = :mes AND YEAR(o.fechaOrden) = :anio")
    Long countByMes(@Param("mes") Integer mes, @Param("anio") Integer anio);

    @Query("SELECT o FROM Orden o WHERE MONTH(o.fechaOrden) = :mes AND YEAR(o.fechaOrden) = :anio")
    List<Orden> findByMes(@Param("mes") Integer mes, @Param("anio") Integer anio);

}