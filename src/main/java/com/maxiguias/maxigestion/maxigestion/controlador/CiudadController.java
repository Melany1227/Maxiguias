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

import com.maxiguias.maxigestion.maxigestion.modelo.Ciudad;
import com.maxiguias.maxigestion.maxigestion.servicio.CiudadService;

@Controller
@RequestMapping("/ciudades")
public class CiudadController {

    private final CiudadService ciudadService;

    public CiudadController(CiudadService ciudadService) {
        this.ciudadService = ciudadService;
    }

    @GetMapping
    public String listarCiudades(Model model) {
        List<Ciudad> ciudades = ciudadService.listarCiudades();
        model.addAttribute("ciudades", ciudades);
        return "ciudades/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("ciudad", new Ciudad());
        return "ciudades/formulario";
    }

    @GetMapping("/{id}")
    public String obtenerCiudadPorId(@PathVariable Long id, Model model) {
        Optional<Ciudad> ciudad = ciudadService.obtenerCiudadPorId(id);
        if (ciudad.isPresent()) {
            model.addAttribute("ciudad", ciudad.get());
            return "ciudades/detalle";
        } else {
            return "redirect:/ciudades";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Ciudad> ciudad = ciudadService.obtenerCiudadPorId(id);
        if (ciudad.isPresent()) {
            model.addAttribute("ciudad", ciudad.get());
            return "ciudades/formulario";
        } else {
            return "redirect:/ciudades";
        }
    }

    @PostMapping
    public String crearCiudad(@ModelAttribute Ciudad ciudad, RedirectAttributes redirectAttributes) {
        ciudadService.guardarCiudad(ciudad);
        redirectAttributes.addFlashAttribute("mensaje", "Ciudad creada exitosamente");
        return "redirect:/ciudades";
    }

    @PostMapping("/{id}")
    public String actualizarCiudad(@PathVariable Long id, @ModelAttribute Ciudad ciudad, RedirectAttributes redirectAttributes) {
        ciudad.setId(id.intValue());
        ciudadService.guardarCiudad(ciudad);
        redirectAttributes.addFlashAttribute("mensaje", "Ciudad actualizada exitosamente");
        return "redirect:/ciudades";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarCiudad(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ciudadService.eliminarCiudad(id);
        redirectAttributes.addFlashAttribute("mensaje", "Ciudad eliminada exitosamente");
        return "redirect:/ciudades";
    }
}