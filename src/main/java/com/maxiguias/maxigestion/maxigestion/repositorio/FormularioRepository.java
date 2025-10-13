package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxiguias.maxigestion.maxigestion.modelo.Formulario;

@Repository
public interface FormularioRepository extends JpaRepository<Formulario, Long> {

    Optional<Formulario> findByUrl(String url);
    Optional<Formulario> findByNombreFormulario(String nombreFormulario);
}