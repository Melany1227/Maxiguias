package com.maxiguias.maxigestion.maxigestion.repositorio;

import java.util.List;

import com.maxiguias.maxigestion.maxigestion.modelo.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {
    
    List<Ciudad> findByDepartamento_Id(Integer departamentoId);
    
}

