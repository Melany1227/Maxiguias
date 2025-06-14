package com.maxiguias.maxigestion.maxigestion.repositorio;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maxiguias.maxigestion.maxigestion.modelo.Terminado;

public interface TerminadoRepository extends JpaRepository<Terminado, Long> {
    List<Terminado> findByProductoId(Long productoId);
}