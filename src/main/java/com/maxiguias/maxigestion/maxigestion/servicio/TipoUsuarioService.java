package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.TipoUsuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.TipoUsuarioRepository;

@Service
public class TipoUsuarioService {

    private final TipoUsuarioRepository tipoUsuarioRepository;
    
    public TipoUsuarioService(TipoUsuarioRepository tipoUsuarioRepository) {
        this.tipoUsuarioRepository = tipoUsuarioRepository;
    }

    public List<TipoUsuario> obtenerTiposUsuario() {
        return tipoUsuarioRepository.findAll();
    }

}
