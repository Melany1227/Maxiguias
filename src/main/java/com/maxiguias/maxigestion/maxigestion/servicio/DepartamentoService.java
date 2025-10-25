package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.Departamento;
import com.maxiguias.maxigestion.maxigestion.repositorio.DepartamentoRepository;

@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
    }

    public List<Departamento> listarDepartamentos() {
        return departamentoRepository.findAll();
    }

    public Optional<Departamento> obtenerDepartamentoPorId(Integer id) {
        return departamentoRepository.findById(id);
    }

    public Departamento guardarDepartamento(Departamento departamento) {
        return departamentoRepository.save(departamento);
    }

    public void eliminarDepartamento(Integer id) {
        departamentoRepository.deleteById(id);
    }
}