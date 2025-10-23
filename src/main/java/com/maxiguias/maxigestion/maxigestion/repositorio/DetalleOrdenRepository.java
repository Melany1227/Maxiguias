package com.maxiguias.maxigestion.maxigestion.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrdenId;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, DetalleOrdenId> {

    public void deleteByOrdenId(Long ordenId);
}