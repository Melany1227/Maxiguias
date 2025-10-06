package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

   List<Usuario> findByTipoUsuario_NombreIn(List<String> nombres);

   Optional<Usuario> findByNombreUsuario(String nombreUsuario);

   boolean existsByDocumento(Long documento);

   boolean existsByNombreUsuario(String nombreUsuario);

   // Métodos para reportes
   Long countByTipoUsuario_Id(Integer tipoUsuarioId);

   List<Usuario> findByTipoUsuario_Id(Integer tipoUsuarioId);

   // Métodos para reportes con filtro por mes
   @Query("SELECT COUNT(u) FROM Usuario u WHERE u.tipoUsuario.id = :tipoUsuarioId AND MONTH(u.fechaRegistro) = :mes AND YEAR(u.fechaRegistro) = :anio")
   Long countByTipoUsuarioAndMes(@Param("tipoUsuarioId") Integer tipoUsuarioId, @Param("mes") Integer mes, @Param("anio") Integer anio);
   
   @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario.id = :tipoUsuarioId AND MONTH(u.fechaRegistro) = :mes AND YEAR(u.fechaRegistro) = :anio")
   List<Usuario> findByTipoUsuarioAndMes(@Param("tipoUsuarioId") Integer tipoUsuarioId, @Param("mes") Integer mes, @Param("anio") Integer anio);

}
