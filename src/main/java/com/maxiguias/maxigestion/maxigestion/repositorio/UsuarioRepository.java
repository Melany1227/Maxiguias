package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

   List<Usuario> findByTipoUsuario_NombreIn(List<String> nombres);
   
   Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    
}
