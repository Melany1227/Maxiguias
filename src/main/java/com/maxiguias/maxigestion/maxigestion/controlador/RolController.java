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

import com.maxiguias.maxigestion.maxigestion.modelo.Rol;
import com.maxiguias.maxigestion.maxigestion.servicio.RolService;

@Controller
@RequestMapping("/roles")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public String listarRoles(Model model) {
        List<Rol> roles = rolService.listarRoles();
        model.addAttribute("roles", roles);
        return "roles/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("rol", new Rol());
        return "roles/formulario";
    }

    @GetMapping("/{id}")
    public String obtenerRolPorId(@PathVariable Long id, Model model) {
        Optional<Rol> rol = rolService.obtenerRolPorId(id);
        if (rol.isPresent()) {
            model.addAttribute("rol", rol.get());
            return "roles/detalle";
        } else {
            return "redirect:/roles";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Rol> rol = rolService.obtenerRolPorId(id);
        if (rol.isPresent()) {
            model.addAttribute("rol", rol.get());
            return "roles/formulario";
        } else {
            return "redirect:/roles";
        }
    }

    @PostMapping
    public String crearRol(@ModelAttribute Rol rol, RedirectAttributes redirectAttributes) {
        rolService.guardarRol(rol);
        redirectAttributes.addFlashAttribute("mensaje", "Rol creado exitosamente");
        return "redirect:/roles";
    }

    @PostMapping("/{id}")
    public String actualizarRol(@PathVariable Long id, @ModelAttribute Rol rol, RedirectAttributes redirectAttributes) {
        rol.setId(id);
        rolService.guardarRol(rol);
        redirectAttributes.addFlashAttribute("mensaje", "Rol actualizado exitosamente");
        return "redirect:/roles";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarRol(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        rolService.eliminarRol(id);
        redirectAttributes.addFlashAttribute("mensaje", "Rol eliminado exitosamente");
        return "redirect:/roles";
    }
}