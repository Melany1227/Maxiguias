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
import com.maxiguias.maxigestion.maxigestion.modelo.Departamento;
import com.maxiguias.maxigestion.maxigestion.servicio.CiudadService;
import com.maxiguias.maxigestion.maxigestion.servicio.DepartamentoService;

@Controller
@RequestMapping("/ciudades")
public class CiudadController {

    private final CiudadService ciudadService;
    private final DepartamentoService departamentoService;

    public CiudadController(CiudadService ciudadService, DepartamentoService departamentoService) {
        this.ciudadService = ciudadService;
        this.departamentoService = departamentoService;
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
        List<Departamento> departamentos = departamentoService.listarDepartamentos();
        model.addAttribute("departamentos", departamentos);
        return "ciudades/formulario";
    }

    @GetMapping("/{id}")
    public String obtenerCiudadPorId(@PathVariable Integer id, Model model) {
        Optional<Ciudad> ciudad = ciudadService.obtenerCiudadPorId(id);
        if (ciudad.isPresent()) {
            model.addAttribute("ciudad", ciudad.get());
            return "ciudades/detalle";
        } else {
            return "redirect:/ciudades";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Optional<Ciudad> ciudad = ciudadService.obtenerCiudadPorId(id);
        if (ciudad.isPresent()) {
            model.addAttribute("ciudad", ciudad.get());
            List<Departamento> departamentos = departamentoService.listarDepartamentos();
            model.addAttribute("departamentos", departamentos);
            return "ciudades/formulario";
        } else {
            return "redirect:/ciudades";
        }
    }

    @PostMapping
    public String crearCiudad(@ModelAttribute Ciudad ciudad, RedirectAttributes redirectAttributes) {
        if (ciudadService.obtenerCiudadPorNombre(ciudad.getNombre()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "La ciudad ya existe, intenta con otro nombre");
            return "redirect:/ciudades/nuevo";
        }
        ciudadService.guardarCiudad(ciudad);
        redirectAttributes.addFlashAttribute("mensaje", "Ciudad creada exitosamente");
        return "redirect:/ciudades";
    }

    @PostMapping("/{id}")
    public String actualizarCiudad(@PathVariable Integer id, @ModelAttribute Ciudad ciudad, RedirectAttributes redirectAttributes) {
        Optional<Ciudad> ciudadExistente = ciudadService.obtenerCiudadPorNombre(ciudad.getNombre());
        if (ciudadExistente.isPresent() && !ciudadExistente.get().getId().equals(id)) {
            redirectAttributes.addFlashAttribute("error", "La ciudad ya existe, intenta con otro nombre");
            return "redirect:/ciudades/" + id + "/editar";
        }
        ciudad.setId(id);
        ciudadService.guardarCiudad(ciudad);
        redirectAttributes.addFlashAttribute("mensaje", "Ciudad actualizada exitosamente");
        return "redirect:/ciudades";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarCiudad(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        ciudadService.eliminarCiudad(id);
        redirectAttributes.addFlashAttribute("mensaje", "Ciudad eliminada exitosamente");
        return "redirect:/ciudades";
    }
}