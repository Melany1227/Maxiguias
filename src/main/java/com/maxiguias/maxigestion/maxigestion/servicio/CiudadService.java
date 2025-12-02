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

    public List<Ciudad> listarCiudadesPorDepartamento(Integer departamentoId) {
        return ciudadRepository.findByDepartamento_Id(departamentoId);
    }

    public Optional<Ciudad> obtenerCiudadPorId(Integer id) {
        return ciudadRepository.findById(id);
    }

    public Optional<Ciudad> obtenerCiudadPorNombre(String nombre) {
        return ciudadRepository.findByNombre(nombre);
    }

    public Ciudad guardarCiudad(Ciudad ciudad) {
        return ciudadRepository.save(ciudad);
    }

    public void eliminarCiudad(Integer id) {
        ciudadRepository.deleteById(id);
    }
}