package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.FormularioXPerfil;
import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.FormularioXPerfilRepository;

@Service
public class AutorizacionService {

    @Autowired
    private FormularioXPerfilRepository formularioXPerfilRepository;

    public boolean tienePermiso(Usuario usuario, String url, String accion) {
        if (usuario == null || usuario.getPerfil() == null) {
            return false;
        }

        System.out
                .println("DEBUG - Buscando permisos para URL: " + url + ", Perfil ID: " + usuario.getPerfil().getId());

        // Primero intentar coincidencia exacta
        Optional<FormularioXPerfil> permisoOpt = formularioXPerfilRepository
                .findByPerfilIdAndFormularioUrl(usuario.getPerfil().getId(), url);

        // Si no encuentra exacta, buscar por prefijo (para URLs dinámicas)
        if (permisoOpt.isEmpty()) {
            System.out.println("DEBUG - No encontró coincidencia exacta, buscando por prefijo...");
            List<FormularioXPerfil> permisos = formularioXPerfilRepository
                    .findByPerfilIdAndUrlStartsWith(usuario.getPerfil().getId(), url);
            if (!permisos.isEmpty()) {
                permisoOpt = Optional.of(permisos.get(0)); // Tomar el más específico (ordenado por longitud DESC)
            }
        }

        if (permisoOpt.isEmpty()) {
            System.out.println("DEBUG - No se encontraron permisos para la URL: " + url);
            return false;
        }

        FormularioXPerfil permiso = permisoOpt.get();
        System.out.println("DEBUG - Permiso encontrado para formulario: " + permiso.getFormulario().getUrl() +
                ", verificando acción: " + accion);
        return verificarAccion(permiso, accion);
    }

    public boolean tienePermisoFormulario(Usuario usuario, String nombreFormulario, String accion) {
        if (usuario == null || usuario.getPerfil() == null) {
            return false;
        }

        Optional<FormularioXPerfil> permisoOpt = formularioXPerfilRepository
                .findByPerfilIdAndFormularioNombre(usuario.getPerfil().getId(), nombreFormulario);

        if (permisoOpt.isEmpty()) {
            return false;
        }

        FormularioXPerfil permiso = permisoOpt.get();
        return verificarAccion(permiso, accion);
    }

    private boolean verificarAccion(FormularioXPerfil permiso, String accion) {
        switch (accion.toUpperCase()) {
            case "CREAR":
                return permiso.getCrear() == 'S' || permiso.getCrear() == '1';
            case "EDITAR":
                return permiso.getEditar() == 'S' || permiso.getEditar() == '1';
            case "VISUALIZAR":
            case "VER":
                return permiso.getVisualizar() == 'S' || permiso.getVisualizar() == '1';
            case "ELIMINAR":
                return permiso.getEliminar() == 'S' || permiso.getEliminar() == '1';
            default:
                return false;
        }
    }

    public boolean puedeCrear(Usuario usuario, String url) {
        return tienePermiso(usuario, url, "CREAR");
    }

    public boolean puedeEditar(Usuario usuario, String url) {
        return tienePermiso(usuario, url, "EDITAR");
    }

    public boolean puedeVer(Usuario usuario, String url) {
        return tienePermiso(usuario, url, "VISUALIZAR");
    }

    public boolean puedeEliminar(Usuario usuario, String url) {
        return tienePermiso(usuario, url, "ELIMINAR");
    }
}