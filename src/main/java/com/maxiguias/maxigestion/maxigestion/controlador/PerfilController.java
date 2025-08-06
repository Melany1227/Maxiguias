package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.maxiguias.maxigestion.maxigestion.modelo.Perfil;
import com.maxiguias.maxigestion.maxigestion.modelo.Rol;
import com.maxiguias.maxigestion.maxigestion.servicio.PerfilService;
import com.maxiguias.maxigestion.maxigestion.servicio.RolService;

@Controller
@RequestMapping("/perfiles")
public class PerfilController {

    private final PerfilService perfilService;
    private final RolService rolService;

    public PerfilController(PerfilService perfilService, RolService rolService) {
        this.perfilService = perfilService;
        this.rolService = rolService;
    }

    @GetMapping
    public String listarPerfiles(Model model) {
        List<Perfil> perfiles = perfilService.obtenerPerfiles();
        model.addAttribute("perfiles", perfiles);
        return "perfiles/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("perfil", new Perfil());
        List<Rol> roles = rolService.listarRoles();
        model.addAttribute("roles", roles);
        return "perfiles/formulario";
    }

    @GetMapping("/{id}")
    public String obtenerPerfilPorId(@PathVariable Long id, Model model) {
        Optional<Perfil> perfil = perfilService.obtenerPerfilPorId(id);
        if (perfil.isPresent()) {
            model.addAttribute("perfil", perfil.get());
            return "perfiles/detalle";
        } else {
            return "redirect:/perfiles";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Perfil> perfil = perfilService.obtenerPerfilPorId(id);
        if (perfil.isPresent()) {
            model.addAttribute("perfil", perfil.get());
            List<Rol> roles = rolService.listarRoles();
            model.addAttribute("roles", roles);
            return "perfiles/formulario";
        } else {
            return "redirect:/perfiles";
        }
    }

    @PostMapping
    public String crearPerfil(@ModelAttribute Perfil perfil, RedirectAttributes redirectAttributes) {
        perfilService.guardarPerfil(perfil);
        redirectAttributes.addFlashAttribute("mensaje", "Perfil creado exitosamente");
        return "redirect:/perfiles";
    }

    @PostMapping("/{id}")
    public String actualizarPerfil(@PathVariable Long id, @ModelAttribute Perfil perfil, RedirectAttributes redirectAttributes) {
        perfil.setId(id);
        perfilService.guardarPerfil(perfil);
        redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
        return "redirect:/perfiles";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarPerfil(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        perfilService.eliminarPerfil(id);
        redirectAttributes.addFlashAttribute("mensaje", "Perfil eliminado exitosamente");
        return "redirect:/perfiles";
    }
}