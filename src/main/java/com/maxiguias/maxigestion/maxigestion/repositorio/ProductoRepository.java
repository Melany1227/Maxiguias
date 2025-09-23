package com.maxiguias.maxigestion.maxigestion.repositorio;

import com.maxiguias.maxigestion.maxigestion.modelo.Producto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Método personalizado de busqueda
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Producto> buscarPorNombre(@Param("busqueda") String busqueda);

    //Consulta SQL
    @Query(value = "SELECT p.id_producto, p.nombre_guia, " +
           "COALESCE(SUM(df.cantidad_producto), 0) as cantidad_total, " +
           "COALESCE(AVG(df.valor_producto), 0) as precio_promedio " +
           "FROM productos p " +
           "LEFT JOIN detalle_facturas df ON p.id_producto = df.id_producto " +
           "GROUP BY p.id_producto, p.nombre_guia", 
           nativeQuery = true)
    List<Object[]> obtenerProductosConEstadisticas();

}
