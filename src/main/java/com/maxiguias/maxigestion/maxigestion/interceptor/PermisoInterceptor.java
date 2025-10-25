package com.maxiguias.maxigestion.maxigestion.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.servicio.AutorizacionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class PermisoInterceptor implements HandlerInterceptor {

    @Autowired
    private AutorizacionService autorizacionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String uri = request.getRequestURI();
        String metodo = request.getMethod();

        // Permitir acceso libre a recursos estáticos y páginas de autenticación
        if (esRecursoPublico(uri)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            redirigirALogin(request, response);
            return false;
        }

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        System.out.println("DEBUG - URI: " + uri + ", Usuario en sesión: "
                + (usuario != null ? usuario.getNombreUsuario() : "null"));

        if (usuario == null) {
            System.out.println("DEBUG - Usuario null, redirigiendo a login");
            redirigirALogin(request, response);
            return false;
        }

        System.out.println("DEBUG - Usuario: " + usuario.getNombreUsuario() +
                ", Perfil ID: " + (usuario.getPerfil() != null ? usuario.getPerfil().getId() : "null"));

        // Determinar la acción basada en el método HTTP y la URI
        String accion = determinarAccion(metodo, uri);
        System.out.println("DEBUG - Acción determinada: " + accion);

        // Verificar permisos
        boolean tienePermiso = autorizacionService.tienePermiso(usuario, uri, accion);
        System.out.println("DEBUG - ¿Tiene permiso?: " + tienePermiso);

        if (!tienePermiso) {
            System.out.println("DEBUG - Sin permisos, redirigiendo a 403");
            response.sendRedirect("/error/403");
            return false;
        }

        return true;
    }

    private boolean esRecursoPublico(String uri) {
        return uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/img/") ||
                uri.startsWith("/login") ||
                uri.startsWith("/auth/") ||
                uri.startsWith("/api/") ||
                uri.startsWith("/.well-known/") ||
                uri.startsWith("/productos/publico") ||
                uri.equals("/") ||
                uri.startsWith("/error/");
    }

    private void redirigirALogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getHeader("X-Requested-With") != null &&
                request.getHeader("X-Requested-With").equals("XMLHttpRequest")) {
            // Petición AJAX
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            // Petición normal
            response.sendRedirect("/login");
        }
    }

    private String determinarAccion(String metodo, String uri) {
        // Determinar acción basada en patrones de URL
        if (uri.contains("/crear") || uri.contains("/nuevo")) {
            return "CREAR";
        } else if (uri.contains("/editar") || uri.contains("/actualizar") || metodo.equals("POST")) {
            return "EDITAR";
        } else if (uri.contains("/eliminar") || metodo.equals("DELETE")) {
            return "ELIMINAR";
        } else {
            return "VISUALIZAR";
        }
    }
}