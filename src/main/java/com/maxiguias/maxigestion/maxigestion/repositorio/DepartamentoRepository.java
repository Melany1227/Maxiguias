package com.maxiguias.maxigestion.maxigestion.repositorio;

import com.maxiguias.maxigestion.maxigestion.modelo.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {
    Optional<Departamento> findByNombre(String nombre);
}