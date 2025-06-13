package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElse(null); 
    }

    public String crearUsuario(Usuario usuario) {
        String tipo = usuario.getTipoUsuario().getNombre().toUpperCase();

        if (tipo.equals("NATURAL")) {
            if (usuario.getNombreUsuario() != null && !usuario.getNombreUsuario().isEmpty() || usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
                return "Error: Los usuarios NATURALES no deben tener usuario ni contraseña.";
            }
        }

        if (tipo.equals("JURIDICO")) {
            if (usuario.getPrimerApellido() != null || usuario.getSegundoApellido() != null) {
                return "Error: Los usuarios JURÍDICOS no deben tener apellidos.";
            }
        }

        usuarioRepository.save(usuario);
        return "OK";
    }

    public String actualizarUsuario(Usuario usuario) {
        Usuario existente = obtenerPorId(usuario.getDocumento());
        if (existente == null) {
            return "Usuario no encontrado";
        }
        usuarioRepository.save(usuario); 
        return "OK";
    }

    
    public void eliminarPorId(Long id) {
        usuarioRepository.deleteById(id);
    }


}
