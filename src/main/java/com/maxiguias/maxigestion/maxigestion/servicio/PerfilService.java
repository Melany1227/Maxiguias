package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.Perfil;
import com.maxiguias.maxigestion.maxigestion.repositorio.PerfilRepository;

@Service
public class PerfilService {

    private final PerfilRepository perfilRepository;

    public PerfilService(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository;
    }

    public List<Perfil> obtenerPerfiles() {
        return perfilRepository.findAll();
    }

    public Optional<Perfil> obtenerPerfilPorId(Long id) {
        return perfilRepository.findById(id);
    }

    public Perfil guardarPerfil(Perfil perfil) {
        return perfilRepository.save(perfil);
    }

    public void eliminarPerfil(Long id) {
        perfilRepository.deleteById(id);
    }
}
