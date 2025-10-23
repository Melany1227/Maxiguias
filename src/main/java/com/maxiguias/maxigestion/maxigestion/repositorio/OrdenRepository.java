package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.maxiguias.maxigestion.maxigestion.modelo.Orden;

public interface OrdenRepository extends JpaRepository<Orden, Long>, PagingAndSortingRepository<Orden, Long> {

    // Método para contar todas las órdenes
    Long countBy();

    // Métodos para reportes con filtro por mes
    @Query("SELECT COUNT(o) FROM Orden o WHERE MONTH(o.fechaOrden) = :mes AND YEAR(o.fechaOrden) = :anio")
    Long countByMes(@Param("mes") Integer mes, @Param("anio") Integer anio);

    @Query("SELECT o FROM Orden o WHERE MONTH(o.fechaOrden) = :mes AND YEAR(o.fechaOrden) = :anio")
    List<Orden> findByMes(@Param("mes") Integer mes, @Param("anio") Integer anio);

    // Métodos para filtro por cliente con paginación
    @Query("SELECT o FROM Orden o WHERE o.usuario.documento = :clienteDocumento")
    Page<Orden> findByUsuarioDocumento(@Param("clienteDocumento") Long clienteDocumento, Pageable pageable);

    // Método para buscar por cliente con nombre o documento
    @Query("SELECT o FROM Orden o WHERE " +
           "LOWER(CONCAT(o.usuario.nombre, ' ', o.usuario.primerApellido)) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "STR(o.usuario.documento) LIKE CONCAT('%', :termino, '%')")
    Page<Orden> findByClienteTermino(@Param("termino") String termino, Pageable pageable);

}