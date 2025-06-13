package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.servicio.PerfilService;
import com.maxiguias.maxigestion.maxigestion.servicio.TipoUsuarioService;
import com.maxiguias.maxigestion.maxigestion.servicio.UsuarioService;

@Controller
@RequestMapping("/usuarios")

public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PerfilService perfilService;

    @Autowired
    private TipoUsuarioService tipoUsuarioService;

    @GetMapping()
    public String listarUsuarios(Model model) {
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "listar_usuarios"; 
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrearUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tiposUsuario", tipoUsuarioService.obtenerTiposUsuario()); // Lista desde la BD
        model.addAttribute("perfiles", perfilService.obtenerPerfiles()); 
        return "crear_usuario";
    }

    @PostMapping()
    public String crearUsuario(@ModelAttribute Usuario usuario, Model model) {
        String resultado = usuarioService.crearUsuario(usuario);

        if (!resultado.equals("OK")) {
            model.addAttribute("usuario", usuario); 
            model.addAttribute("mensajeError", resultado);
            return "crear_usuario";
        }

        return listarUsuarios(model);
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id); 
        model.addAttribute("usuario", usuario);
        model.addAttribute("tiposUsuario", tipoUsuarioService.obtenerTiposUsuario());
        model.addAttribute("perfiles", perfilService.obtenerPerfiles());
        return "crear_usuario"; 
    }

    @PostMapping("/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuario, Model model) {
        String resultado = usuarioService.actualizarUsuario(usuario); 
        if (!resultado.equals("OK")) {
            model.addAttribute("mensajeError", resultado);
            model.addAttribute("tiposUsuario", tipoUsuarioService.obtenerTiposUsuario());
            model.addAttribute("perfiles", perfilService.obtenerPerfiles());
            return "crear_usuario";
        }
        return listarUsuarios(model);
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(Model model, @PathVariable Long id) {
        usuarioService.eliminarPorId(id); 
        return listarUsuarios(model);
    }



}
