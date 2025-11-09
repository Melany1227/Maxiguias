package com.maxiguias.maxigestion.maxigestion.configuracion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            // Buscar el usuario por nombreUsuario
            Usuario usuario = usuarioRepository.findByNombreUsuario(authentication.getName())
                .orElse(null);
            
            if (usuario != null) {
                // Crear el nombre completo para mostrar
                StringBuilder nombreCompleto = new StringBuilder();
                nombreCompleto.append(usuario.getNombre());
                
                if (usuario.getPrimerApellido() != null && !usuario.getPrimerApellido().trim().isEmpty()) {
                    nombreCompleto.append(" ").append(usuario.getPrimerApellido());
                }
                
                model.addAttribute("nombreUsuarioActual", nombreCompleto.toString());
            }
        }
    }
}