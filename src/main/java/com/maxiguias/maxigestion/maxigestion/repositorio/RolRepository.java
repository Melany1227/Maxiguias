package com.maxiguias.maxigestion.maxigestion.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;



import com.maxiguias.maxigestion.maxigestion.modelo.Rol;



import java.util.Optional;



public interface RolRepository extends JpaRepository<Rol, Long> {



    Optional<Rol> findByNombreRol(String nombreRol);



}
