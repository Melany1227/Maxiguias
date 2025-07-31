package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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
            boolean tieneUsuario = usuario.getNombreUsuario() != null && !usuario.getNombreUsuario().trim().isEmpty();
            boolean tieneContrasena = usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty();

            if (tieneUsuario || tieneContrasena) {
                return "Error: Los clientes naturales no deben tener usuario ni contraseña.";
            }
        }

        if (tipo.equals("JURIDICO")) {
            boolean tienePrimerApellido = usuario.getPrimerApellido() != null && !usuario.getPrimerApellido().trim().isEmpty();
            boolean tieneSegundoApellido = usuario.getSegundoApellido() != null && !usuario.getSegundoApellido().trim().isEmpty();

            if (tienePrimerApellido || tieneSegundoApellido) {
                return "Error: Los clientes jurídicos no deben tener apellidos.";
            }
        }

        // Encriptar contraseña si existe
        if (usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty()) {
            usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        }

        usuarioRepository.save(usuario);
        return "Usuario guardado exitosamente.";
    }

    public String actualizarUsuario(Usuario usuario) {
        Usuario existente = obtenerPorId(usuario.getDocumento());
        if (existente == null) {
            return "Error: Usuario no encontrado";
        }else{
            String tipo = usuario.getTipoUsuario().getNombre().toUpperCase();

            if (tipo.equals("NATURAL")) {
                boolean tieneUsuario = usuario.getNombreUsuario() != null && !usuario.getNombreUsuario().trim().isEmpty();
                boolean tieneContrasena = usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty();

                if (tieneUsuario || tieneContrasena) {
                    return "Error: Los clientes naturales no deben tener usuario ni contraseña.";
                }
            }

            if (tipo.equals("JURIDICO")) {
                boolean tienePrimerApellido = usuario.getPrimerApellido() != null && !usuario.getPrimerApellido().trim().isEmpty();
                boolean tieneSegundoApellido = usuario.getSegundoApellido() != null && !usuario.getSegundoApellido().trim().isEmpty();

                if (tienePrimerApellido || tieneSegundoApellido) {
                    return "Error: Los clientes jurídicos no deben tener apellidos.";
                }
            }
            
            // Encriptar contraseña si se está actualizando
            if (usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty()) {
                usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
            } else {
                // Mantener la contraseña existente si no se proporciona una nueva
                usuario.setContrasena(existente.getContrasena());
            }
            
            usuarioRepository.save(usuario); 
            return "Usuario actualizado exitosamente.";
        }
       
    }

    
    public void eliminarPorId(Long id) {
        usuarioRepository.deleteById(id);
    }

    public String encriptarContrasena(String contrasenaPlana) {
        return passwordEncoder.encode(contrasenaPlana);
    }

    public boolean validarContrasena(String contrasenaPlana, String contrasenaEncriptada) {
        return passwordEncoder.matches(contrasenaPlana, contrasenaEncriptada);
    }

}
