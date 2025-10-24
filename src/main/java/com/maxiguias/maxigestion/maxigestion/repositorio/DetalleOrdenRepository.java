package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrdenId;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, DetalleOrdenId> {

    public void deleteByOrdenId(Long ordenId);
    
    // Consulta para obtener productos más vendidos con sus medidas por mes
    @Query("SELECT p.nombre, t.medidaTerminadoProducto, SUM(d.cantidad) as totalVendido " +
           "FROM DetalleOrden d " +
           "JOIN d.terminado t " +
           "JOIN t.producto p " +
           "JOIN d.orden o " +
           "WHERE MONTH(o.fechaOrden) = :mes AND YEAR(o.fechaOrden) = :anio " +
           "GROUP BY p.nombre, t.medidaTerminadoProducto " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductosMasVendidosPorMes(@Param("mes") Integer mes, @Param("anio") Integer anio);
    
    // Consulta para obtener productos más vendidos con sus medidas (todos los tiempos)
    @Query("SELECT p.nombre, t.medidaTerminadoProducto, SUM(d.cantidad) as totalVendido " +
           "FROM DetalleOrden d " +
           "JOIN d.terminado t " +
           "JOIN t.producto p " +
           "GROUP BY p.nombre, t.medidaTerminadoProducto " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findProductosMasVendidos();
    
    // Contar total de productos vendidos por mes
    @Query("SELECT SUM(d.cantidad) FROM DetalleOrden d JOIN d.orden o " +
           "WHERE MONTH(o.fechaOrden) = :mes AND YEAR(o.fechaOrden) = :anio")
    Long countTotalProductosVendidosPorMes(@Param("mes") Integer mes, @Param("anio") Integer anio);
    
    // Contar total de productos vendidos (todos los tiempos)
    @Query("SELECT SUM(d.cantidad) FROM DetalleOrden d")
    Long countTotalProductosVendidos();
}