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

    // ========== MÉTODOS PARA REPORTE DE PRODUCTOS MÁS VENDIDOS ==========

    // Contar total de productos vendidos (suma de cantidades)
    @Query("SELECT SUM(d.cantidad) FROM DetalleOrden d INNER JOIN d.orden o")
    Long countTotalProductosVendidos();

    // Contar total de productos vendidos por mes
    @Query("SELECT SUM(d.cantidad) FROM DetalleOrden d INNER JOIN d.orden o WHERE MONTH(o.fechaOrden) = :mes AND YEAR(o.fechaOrden) = :anio")
    Long countTotalProductosVendidosByMes(@Param("mes") Integer mes, @Param("anio") Integer anio);

    // Obtener productos más vendidos ordenados por cantidad (para exportar Excel)
    @Query("SELECT p.nombre, SUM(d.cantidad) FROM DetalleOrden d " +
           "INNER JOIN d.orden o " +
           "INNER JOIN d.producto p " +
           "GROUP BY p.id, p.nombre " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductosMasVendidos();

    // Obtener productos más vendidos por mes ordenados por cantidad (para exportar Excel)
    @Query("SELECT p.nombre, SUM(d.cantidad) FROM DetalleOrden d " +
           "INNER JOIN d.orden o " +
           "INNER JOIN d.producto p " +
           "WHERE MONTH(o.fechaOrden) = :mes AND YEAR(o.fechaOrden) = :anio " +
           "GROUP BY p.id, p.nombre " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductosMasVendidosByMes(@Param("mes") Integer mes, @Param("anio") Integer anio);

}