package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxiguias.maxigestion.maxigestion.modelo.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Búsqueda simple por nombre (lista completa)
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%'))")
    List<Producto> buscarPorNombre(@Param("busqueda") String busqueda);

    // Consulta nativa con estadísticas
    @Query(value = "SELECT p.id_producto, p.nombre_guia, " +
            "COALESCE(SUM(df.cantidad_producto), 0) as cantidad_total, " +
            "COALESCE(AVG(df.valor_producto), 0) as precio_promedio " +
            "FROM productos p " +
            "LEFT JOIN detalle_facturas df ON p.id_producto = df.id_producto " +
            "GROUP BY p.id_producto, p.nombre_guia", nativeQuery = true)
    List<Object[]> obtenerProductosConEstadisticas();

    // Búsqueda flexible por ID o nombre
    @Query("SELECT p FROM Producto p WHERE " +
            "CAST(p.id AS string) LIKE %:keyword% OR " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Producto> buscarPorCodigoONombre(@Param("keyword") String keyword, Pageable pageable);

}
