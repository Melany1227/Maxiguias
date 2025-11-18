package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.maxiguias.maxigestion.maxigestion.modelo.Formulario;
import com.maxiguias.maxigestion.maxigestion.repositorio.FormularioRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.FormularioXPerfilRepository;

@Controller
@RequestMapping("/formularios")
public class FormularioController {

    @Autowired
    private FormularioRepository formularioRepository;

    @Autowired
    private FormularioXPerfilRepository formularioXPerfilRepository;

    // Listar todos los formularios
    @GetMapping
    public String listarFormularios(Model model) {
        List<Formulario> formularios = formularioRepository.findAll();
        model.addAttribute("formularios", formularios);
        return "formularios/listar-formularios";
    }

    // Formulario para crear nuevo formulario
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        return "formularios/crear-formulario";
    }

    // Guardar nuevo formulario
    @PostMapping("/nuevo")
    public String crearFormulario(
            @RequestParam String nombreFormulario,
            @RequestParam String url,
            RedirectAttributes ra) {

        // Validar que no exista otro formulario con la misma URL
        if (formularioRepository.findByUrl(url).isPresent()) {
            ra.addFlashAttribute("error", "Ya existe un formulario con esta URL");
            return "redirect:/formularios/nuevo";
        }

        // Validar que no exista otro formulario con el mismo nombre
        if (formularioRepository.findByNombreFormulario(nombreFormulario).isPresent()) {
            ra.addFlashAttribute("error", "Ya existe un formulario con este nombre");
            return "redirect:/formularios/nuevo";
        }

        Formulario formulario = new Formulario();
        formulario.setNombreFormulario(nombreFormulario);
        formulario.setUrl(url);

        formularioRepository.save(formulario);
        ra.addFlashAttribute("mensajeExito", "Formulario creado correctamente");

        return "redirect:/formularios";
    }

    // Formulario para editar formulario
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            Model model) {

        Optional<Formulario> formulario = formularioRepository.findById(id);

        if (!formulario.isPresent()) {
            return "redirect:/formularios";
        }

        model.addAttribute("formulario", formulario.get());

        return "formularios/editar-formulario";
    }

    // Actualizar formulario
    @PostMapping("/editar/{id}")
    public String actualizarFormulario(
            @PathVariable Long id,
            @RequestParam String nombreFormulario,
            @RequestParam String url,
            RedirectAttributes ra) {

        Optional<Formulario> formularioOptional = formularioRepository.findById(id);

        if (!formularioOptional.isPresent()) {
            ra.addFlashAttribute("error", "Formulario no encontrado");
            return "redirect:/formularios";
        }

        Formulario formulario = formularioOptional.get();

        // Validar URL única (excepto la del formulario actual)
        Optional<Formulario> formularioConMismaUrl = formularioRepository.findByUrl(url);
        if (formularioConMismaUrl.isPresent() && !formularioConMismaUrl.get().getId().equals(id)) {
            ra.addFlashAttribute("error", "Ya existe otro formulario con esta URL");
            return "redirect:/formularios/editar/" + id;
        }

        // Validar nombre único (excepto el del formulario actual)
        Optional<Formulario> formularioConMismoNombre = formularioRepository.findByNombreFormulario(nombreFormulario);
        if (formularioConMismoNombre.isPresent() && !formularioConMismoNombre.get().getId().equals(id)) {
            ra.addFlashAttribute("error", "Ya existe otro formulario con este nombre");
            return "redirect:/formularios/editar/" + id;
        }

        formulario.setNombreFormulario(nombreFormulario);
        formulario.setUrl(url);

        formularioRepository.save(formulario);
        ra.addFlashAttribute("mensajeExito", "Formulario actualizado correctamente");

        return "redirect:/formularios";
    }

    // Eliminar formulario
    @PostMapping("/eliminar/{id}")
    public String eliminarFormulario(
            @PathVariable Long id,
            RedirectAttributes ra) {

        Optional<Formulario> formulario = formularioRepository.findById(id);

        if (!formulario.isPresent()) {
            ra.addFlashAttribute("error", "Formulario no encontrado");
            return "redirect:/formularios";
        }

        // Eliminar todas las asignaciones formulario_x_perfil relacionadas (cascada)
        formularioXPerfilRepository.deleteByFormularioId(id);

        // Eliminar el formulario
        formularioRepository.deleteById(id);

        ra.addFlashAttribute("mensajeExito", "Formulario eliminado correctamente");

        return "redirect:/formularios";
    }
}
