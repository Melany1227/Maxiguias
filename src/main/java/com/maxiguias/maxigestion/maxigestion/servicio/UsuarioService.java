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
        // Validar formato del documento (solo números)
        String documentoValidation = validarFormatoDocumento(usuario.getDocumento());
        if (documentoValidation != null) {
            return documentoValidation;
        }

        // Validar formato del teléfono
        String telefonoValidation = validarFormatoTelefono(usuario.getTelefono());
        if (telefonoValidation != null) {
            return telefonoValidation;
        }

        // Validar política de contraseñas (solo si se proporciona contraseña)
        if (usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty()) {
            String contrasenaValidation = validarPoliticaContrasena(usuario.getContrasena());
            if (contrasenaValidation != null) {
                return contrasenaValidation;
            }
        }

        // Validar si el documento ya existe (solo si el documento no es null o 0)
        if (usuario.getDocumento() != null && usuario.getDocumento() > 0 && usuarioRepository.existsByDocumento(usuario.getDocumento())) {
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
        } else {
            // Validar formato del documento (solo números)
            String documentoValidation = validarFormatoDocumento(usuario.getDocumento());
            if (documentoValidation != null) {
                return documentoValidation;
            }
            
            // Validar formato del teléfono
            String telefonoValidation = validarFormatoTelefono(usuario.getTelefono());
            if (telefonoValidation != null) {
                return telefonoValidation;
            }
            
            // Validar política de contraseñas (solo si se proporciona nueva contraseña)
            if (usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty()) {
                String contrasenaValidation = validarPoliticaContrasena(usuario.getContrasena());
                if (contrasenaValidation != null) {
                    return contrasenaValidation;
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


    public String eliminarPorId(Long id) {
        try {
            usuarioRepository.deleteById(id);
            return "Usuario eliminado exitosamente.";
        } catch (DataIntegrityViolationException e) {
            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("ordenes")) {
                return "Error: No se puede eliminar este usuario porque tiene órdenes asociadas.";
            } else {
                return "Error: No se puede eliminar este usuario porque tiene datos asociados en el sistema que lo impiden.";
            }
        } catch (Exception e) {
            return "Error: Ocurrió un error al intentar eliminar el usuario: " + e.getMessage();
        }
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
        
        // Validar política de contraseñas para la nueva contraseña
        String contrasenaValidation = validarPoliticaContrasena(nuevaContrasena);
        if (contrasenaValidation != null) {
            return contrasenaValidation;
        }
        
        // Encriptar y guardar la nueva contraseña
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
        
        return "Contraseña cambiada exitosamente.";
    }
    
    /**
     * Valida que el documento contenga solo números
     */
    private String validarFormatoDocumento(Long documento) {
        if (documento == null) {
            return "Error: El documento es obligatorio.";
        }
        
        String documentoStr = documento.toString();
        
        // Verificar que no esté vacío
        if (documentoStr.trim().isEmpty()) {
            return "Error: El documento no puede estar vacío.";
        }
        
        // Verificar longitud mínima y máxima
        if (documentoStr.length() < 6) {
            return "Error: El documento debe tener al menos 6 dígitos.";
        }
        
        if (documentoStr.length() > 15) {
            return "Error: El documento no puede tener más de 15 dígitos.";
        }
        
        // Verificar que solo contenga números (ya que es Long, esto se valida automáticamente)
        // Pero agregamos una validación adicional por si acaso
        try {
            Long.parseLong(documentoStr);
        } catch (NumberFormatException e) {
            return "Error: El documento solo puede contener números. No se permiten puntos, guiones, espacios o letras.";
        }
        
        return null; // Sin errores
    }
    
    /**
     * Valida que el teléfono tenga el formato correcto
     */
    private String validarFormatoTelefono(Long telefono) {
        if (telefono == null) {
            return "Error: El teléfono es obligatorio.";
        }
        
        String telefonoStr = telefono.toString();
        
        // Verificar que no esté vacío
        if (telefonoStr.trim().isEmpty()) {
            return "Error: El teléfono no puede estar vacío.";
        }
        
        // Verificar longitud mínima (10 dígitos para Colombia)
        if (telefonoStr.length() < 10) {
            return "Error: El teléfono debe tener al menos 10 dígitos.";
        }
        
        // Verificar longitud máxima
        if (telefonoStr.length() > 15) {
            return "Error: El teléfono no puede tener más de 15 dígitos.";
        }
        
        // Verificar que solo contenga números
        try {
            Long.parseLong(telefonoStr);
        } catch (NumberFormatException e) {
            return "Error: El teléfono solo puede contener números. No se permiten espacios, guiones o caracteres especiales.";
        }
        
        return null; // Sin errores
    }
    
    /**
     * Valida que la contraseña cumpla con las políticas de seguridad
     */
    private String validarPoliticaContrasena(String contrasena) {
        if (contrasena == null || contrasena.trim().isEmpty()) {
            return "Error: La contraseña es obligatoria.";
        }
        
        // Verificar longitud mínima (8 caracteres)
        if (contrasena.length() < 8) {
            return "Error: La contraseña no cumple con las políticas de seguridad. Debe tener al menos 8 caracteres.";
        }
        
        // Verificar al menos una letra mayúscula
        if (!contrasena.matches(".*[A-Z].*")) {
            return "Error: La contraseña no cumple con las políticas de seguridad. Debe tener al menos una letra mayúscula.";
        }
        
        // Verificar al menos una letra minúscula
        if (!contrasena.matches(".*[a-z].*")) {
            return "Error: La contraseña no cumple con las políticas de seguridad. Debe tener al menos una letra minúscula.";
        }
        
        // Verificar al menos un número
        if (!contrasena.matches(".*[0-9].*")) {
            return "Error: La contraseña no cumple con las políticas de seguridad. Debe tener al menos un número.";
        }
        
        // Verificar al menos un carácter especial
        if (!contrasena.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return "Error: La contraseña no cumple con las políticas de seguridad. Debe tener al menos un carácter especial (!@#$%^&*).";
        }
        
        return null; // Sin errores - contraseña válida
    }

}
