package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query(value = "SELECT vud.documento, vud.primer_apellido, vud.segundo_apellido, " +
               "vud.usuario, vud.contrasena, vud.tipo_usuario, vud.perfiles_id_perfil, " +
               "vud.nombre, vud.direccion, vud.telefono, " +
               "tu.nombre_tipo_usuario " + 
               "FROM vista_usuarios_detalle vud " +
               "INNER JOIN tipo_usuario tu ON vud.tipo_usuario = tu.id_tipo_usuario " +
               "ORDER BY vud.documento", nativeQuery = true)
    List<Object[]> obtenerUsuariosConDatos();



}
