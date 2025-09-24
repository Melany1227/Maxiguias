package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.Ciudad;
import com.maxiguias.maxigestion.maxigestion.repositorio.CiudadRepository;

@Service
public class CiudadService {

    private final CiudadRepository ciudadRepository;

    public CiudadService(CiudadRepository ciudadRepository) {
        this.ciudadRepository = ciudadRepository;
    }

    public List<Ciudad> listarCiudades() {
        return ciudadRepository.findAll();
    }

    public Optional<Ciudad> obtenerCiudadPorId(int id) {
        return ciudadRepository.findById(id);
    }

    public Ciudad guardarCiudad(Ciudad ciudad) {
        return ciudadRepository.save(ciudad);
    }

    public void eliminarCiudad(int id) {
        ciudadRepository.deleteById(id);
    }
}