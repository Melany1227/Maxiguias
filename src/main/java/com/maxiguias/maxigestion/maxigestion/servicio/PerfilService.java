package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;

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


}
