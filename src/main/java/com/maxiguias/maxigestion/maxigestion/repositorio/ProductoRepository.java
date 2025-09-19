package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maxiguias.maxigestion.maxigestion.modelo.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("SELECT p FROM Producto p WHERE " +
            "CAST(p.id AS string) LIKE %:keyword% OR " +
            "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Producto> findByIdProductoOrNombreGuia(@Param("keyword") String keyword);
}
