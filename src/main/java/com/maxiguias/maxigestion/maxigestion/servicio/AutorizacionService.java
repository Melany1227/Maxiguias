package com.maxiguias.maxigestion.maxigestion.servicio;

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

        Optional<FormularioXPerfil> permisoOpt = formularioXPerfilRepository
                .findByPerfilIdAndFormularioUrl(usuario.getPerfil().getId(), url);

        if (permisoOpt.isEmpty()) {
            return false;
        }

        FormularioXPerfil permiso = permisoOpt.get();
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