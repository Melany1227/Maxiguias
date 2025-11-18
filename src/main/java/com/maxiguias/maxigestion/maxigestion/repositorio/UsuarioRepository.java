package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>, PagingAndSortingRepository<Usuario, Long> {

       List<Usuario> findByTipoUsuario_NombreIn(List<String> nombres);

       @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario.nombre IN :tipos AND " +
                     "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
                     "LOWER(u.primerApellido) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
                     "LOWER(CONCAT(u.nombre, ' ', u.primerApellido)) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
                     "STR(u.documento) LIKE CONCAT('%', :termino, '%'))")
       List<Usuario> buscarUsuariosPorTermino(@Param("tipos") List<String> tipos, @Param("termino") String termino);

       @Query("SELECT u FROM Usuario u WHERE " +
                     "LOWER(u.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
                     "LOWER(u.primerApellido) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
                     "LOWER(u.segundoApellido) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
                     "LOWER(CONCAT(u.nombre, ' ', u.primerApellido)) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
                     "LOWER(CONCAT(u.nombre, ' ', u.primerApellido, ' ', u.segundoApellido)) LIKE LOWER(CONCAT('%', :termino, '%')) OR "
                     +
                     "STR(u.documento) LIKE CONCAT('%', :termino, '%')")
       Page<Usuario> buscarUsuariosPaginados(@Param("termino") String termino, Pageable pageable);

       Optional<Usuario> findByNombreUsuario(String nombreUsuario);

       boolean existsByDocumento(Long documento);

       boolean existsByNombreUsuario(String nombreUsuario);

       boolean existsByCorreo(String correo);

       Optional<Usuario> findByCorreo(String correo);

       // Métodos para reportes
       Long countByTipoUsuario_Id(Integer tipoUsuarioId);

       List<Usuario> findByTipoUsuario_Id(Integer tipoUsuarioId);

       // Métodos para reportes con filtro por mes
       @Query("SELECT COUNT(u) FROM Usuario u WHERE u.fechaRegistro BETWEEN :inicio AND :fin")
       Long countByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       @Query("SELECT COUNT(u) FROM Usuario u WHERE u.fechaRegistro BETWEEN :inicio AND :fin AND u.tipoUsuario.id = 2")
       Long countNaturalesByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       @Query("SELECT COUNT(u) FROM Usuario u WHERE u.fechaRegistro BETWEEN :inicio AND :fin AND u.tipoUsuario.id = 3")
       Long countJuridicosByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario.id = 2 AND u.fechaRegistro BETWEEN :inicio AND :fin")
       List<Usuario> findNaturalesByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       @Query("SELECT u FROM Usuario u WHERE u.tipoUsuario.id = 3 AND u.fechaRegistro BETWEEN :inicio AND :fin")
       List<Usuario> findJuridicosByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio,
                     @Param("fin") LocalDateTime fin);

       // Métodos para administrador de perfiles
       long countByPerfil_Id(Long perfilId);

}
