package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrdenId;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, DetalleOrdenId> {

       public void deleteByOrdenId(Long ordenId);

       // Verificar si un terminado está en uso en alguna orden
       @Query("SELECT COUNT(d) > 0 FROM DetalleOrden d WHERE d.terminado.id = :terminadoId")
       boolean existsByTerminadoId(@Param("terminadoId") Long terminadoId);

       // Total de productos vendidos (sin filtro)
       @Query("SELECT SUM(d.cantidad) FROM DetalleOrden d")
       Long countTotalProductosVendidos();

       // Total de productos vendidos rango fechas (nuevo)
       @Query("SELECT SUM(d.cantidad) FROM DetalleOrden d WHERE d.orden.fechaEntrega BETWEEN :inicio AND :fin")
       Long countTotalProductosVendidosEntreFechas(
                     @Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       // Productos más vendidos (sin filtro)
       @Query("""
                         SELECT p.nombre, t.medidaTerminadoProducto, SUM(d.cantidad)
                         FROM DetalleOrden d
                         JOIN d.terminado t
                         JOIN t.producto p
                         GROUP BY p.nombre, t.medidaTerminadoProducto
                         ORDER BY SUM(d.cantidad) DESC
                     """)
       List<Object[]> findProductosMasVendidos();

       // Productos más vendidos rango fechas (nuevo)
       @Query("""
                         SELECT p.nombre, t.medidaTerminadoProducto, SUM(d.cantidad)
                         FROM DetalleOrden d
                         JOIN d.terminado t
                         JOIN t.producto p
                         JOIN d.orden o
                         WHERE o.fechaEntrega BETWEEN :inicio AND :fin
                         GROUP BY p.nombre, t.medidaTerminadoProducto
                         ORDER BY SUM(d.cantidad) DESC
                     """)
       List<Object[]> findProductosMasVendidosEntreFechas(
                     @Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);
}
