package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
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

    public Page<Usuario> obtenerUsuariosPaginados(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Page<Usuario> buscarUsuariosPaginados(String termino, Pageable pageable) {
        return usuarioRepository.buscarUsuariosPaginados(termino, pageable);
    }

    public Usuario obtenerPorId(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElse(null); 
    }

    public String crearUsuario(Usuario usuario) {
        // Validar si el documento ya existe
        if (usuarioRepository.existsByDocumento(usuario.getDocumento())) {
            return "Error: Ya existe un usuario con el documento " + usuario.getDocumento() + ".";
        }
        
        // Validar si el nombre de usuario ya existe (solo si se proporciona)
        if (usuario.getNombreUsuario() != null && !usuario.getNombreUsuario().trim().isEmpty()) {
            if (usuarioRepository.existsByNombreUsuario(usuario.getNombreUsuario())) {
                return "Error: Ya existe un usuario con el nombre de usuario '" + usuario.getNombreUsuario() + "'.";
            }
        }
        
        // Validar si el correo ya existe (solo si se proporciona)
        if (usuario.getCorreo() != null && !usuario.getCorreo().trim().isEmpty()) {
            if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
                return "Error: Ya existe un usuario con el correo '" + usuario.getCorreo() + "'.";
            }
        }
        
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

        try {
            usuarioRepository.save(usuario);
            return "Usuario guardado exitosamente.";
        } catch (DataIntegrityViolationException e) {
            // Manejar violaciones de restricciones únicas no capturadas por las validaciones previas
            String mensaje = e.getMessage();
            if (mensaje.contains("UK3m5n1w5trapxlbo2s42ugwdmd") || mensaje.contains("nombreUsuario")) {
                return "Error: Ya existe un usuario con ese nombre de usuario.";
            } else if (mensaje.contains("documento")) {
                return "Error: Ya existe un usuario con ese documento.";
            } else {
                return "Error: No se pudo guardar el usuario debido a datos duplicados.";
            }
        }
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

    public String generarNuevaContrasena() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder contrasena = new StringBuilder();
        
        for (int i = 0; i < 8; i++) {
            contrasena.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        
        return contrasena.toString();
    }

    public String restablecerContrasena(String correo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        
        if (usuarioOpt.isEmpty()) {
            return "Error: No existe un usuario con ese correo electrónico.";
        }
        
        Usuario usuario = usuarioOpt.get();
        String nuevaContrasena = generarNuevaContrasena();
        
        // Encriptar y guardar la nueva contraseña
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
        
        return nuevaContrasena; // Retorna la contraseña en texto plano para enviar por correo
    }

    public String cambiarContrasenaTemporal(String correo, String contrasenaTemporal, String nuevaContrasena) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        
        if (usuarioOpt.isEmpty()) {
            return "Error: No existe un usuario con ese correo electrónico.";
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Validar que la contraseña temporal sea correcta
        if (!passwordEncoder.matches(contrasenaTemporal, usuario.getContrasena())) {
            return "Error: La contraseña temporal es incorrecta.";
        }
        
        // Validar que la nueva contraseña tenga al menos 6 caracteres
        if (nuevaContrasena.length() < 6) {
            return "Error: La nueva contraseña debe tener al menos 6 caracteres.";
        }
        
        // Encriptar y guardar la nueva contraseña
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
        
        return "Contraseña cambiada exitosamente.";
    }

}
