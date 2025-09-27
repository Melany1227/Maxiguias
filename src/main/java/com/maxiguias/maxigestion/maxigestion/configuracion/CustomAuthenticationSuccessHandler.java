package com.maxiguias.maxigestion.maxigestion.configuracion;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        HttpSession session = request.getSession();
        String nombreUsuario = authentication.getName();
        
        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario).orElse(null);
        
        if (usuario != null) {
            // Guardar el usuario completo en la sesión
            session.setAttribute("usuario", usuario);
            System.out.println("Usuario guardado en sesión: " + usuario.getNombreUsuario() + 
                             " con perfil ID: " + (usuario.getPerfil() != null ? usuario.getPerfil().getId() : "null"));
        } else {
            System.out.println("Usuario no encontrado en BD: " + nombreUsuario);
        }
        
        // Redirigir al dashboard
        response.sendRedirect("/");
    }
}