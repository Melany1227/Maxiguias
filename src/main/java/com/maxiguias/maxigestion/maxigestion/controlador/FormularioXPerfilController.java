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
import com.maxiguias.maxigestion.maxigestion.modelo.FormularioXPerfil;
import com.maxiguias.maxigestion.maxigestion.modelo.FormularioXPerfilId;
import com.maxiguias.maxigestion.maxigestion.modelo.Perfil;
import com.maxiguias.maxigestion.maxigestion.repositorio.FormularioRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.FormularioXPerfilRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.PerfilRepository;

@Controller
@RequestMapping("/formularios-x-perfiles")
public class FormularioXPerfilController {

    @Autowired
    private FormularioXPerfilRepository formularioXPerfilRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private FormularioRepository formularioRepository;

    // Listar perfiles con opción de ver sus formularios
    @GetMapping
    public String listarPerfiles(Model model) {
        List<Perfil> perfiles = perfilRepository.findAll();
        model.addAttribute("perfiles", perfiles);
        return "formularios-x-perfiles/listar-perfiles";
    }

    // Ver formularios de un perfil específico
    @GetMapping("/{perfilId}")
    public String verFormulariosPorPerfil(@PathVariable Long perfilId, Model model) {
        Optional<Perfil> perfil = perfilRepository.findById(perfilId);

        if (!perfil.isPresent()) {
            return "redirect:/formularios-x-perfiles";
        }

        List<FormularioXPerfil> formularios = formularioXPerfilRepository.findByPerfilId(perfilId);
        model.addAttribute("perfil", perfil.get());
        model.addAttribute("formularios", formularios);

        return "formularios-x-perfiles/ver-formularios";
    }

    // Formulario para asignar nuevo formulario a un perfil
    @GetMapping("/{perfilId}/nuevo")
    public String mostrarFormularioNuevo(@PathVariable Long perfilId, Model model) {
        Optional<Perfil> perfil = perfilRepository.findById(perfilId);

        if (!perfil.isPresent()) {
            return "redirect:/formularios-x-perfiles";
        }

        // Obtener formularios ya asignados al perfil
        List<FormularioXPerfil> formulariosAsignados = formularioXPerfilRepository.findByPerfilId(perfilId);
        List<Long> idsAsignados = formulariosAsignados.stream()
                .map(fxp -> fxp.getFormulario().getId())
                .toList();

        // Obtener todos los formularios disponibles
        List<Formulario> todosFormularios = formularioRepository.findAll();

        // Filtrar los formularios que no estén asignados
        List<Formulario> formulariosDisponibles = todosFormularios.stream()
                .filter(f -> !idsAsignados.contains(f.getId()))
                .toList();

        model.addAttribute("perfil", perfil.get());
        model.addAttribute("formularios", formulariosDisponibles);

        return "formularios-x-perfiles/asignar-formulario";
    }

    // Guardar nueva asignación de formulario a perfil
    @PostMapping("/{perfilId}/nuevo")
    public String asignarFormulario(
            @PathVariable Long perfilId,
            @RequestParam Long formularioId,
            @RequestParam char crear,
            @RequestParam char editar,
            @RequestParam char visualizar,
            @RequestParam char eliminar,
            RedirectAttributes ra) {

        Optional<Perfil> perfil = perfilRepository.findById(perfilId);
        Optional<Formulario> formulario = formularioRepository.findById(formularioId);

        if (!perfil.isPresent() || !formulario.isPresent()) {
            ra.addFlashAttribute("error", "Perfil o Formulario no encontrado");
            return "redirect:/formularios-x-perfiles";
        }

        // Verificar si ya existe la asignación
        FormularioXPerfilId id = new FormularioXPerfilId();
        id.setPerfilId(perfilId);
        id.setFormularioId(formularioId);

        Optional<FormularioXPerfil> existente = formularioXPerfilRepository.findById(id);
        if (existente.isPresent()) {
            ra.addFlashAttribute("error", "Este formulario ya está asignado a este perfil");
            return "redirect:/formularios-x-perfiles/" + perfilId + "/nuevo";
        }

        // Crear nueva asignación
        FormularioXPerfil asignacion = new FormularioXPerfil();
        asignacion.setId(id);
        asignacion.setPerfil(perfil.get());
        asignacion.setFormulario(formulario.get());
        asignacion.setCrear(crear);
        asignacion.setEditar(editar);
        asignacion.setVisualizar(visualizar);
        asignacion.setEliminar(eliminar);

        formularioXPerfilRepository.save(asignacion);
        ra.addFlashAttribute("mensajeExito", "Formulario asignado correctamente al perfil");

        return "redirect:/formularios-x-perfiles/" + perfilId;
    }

    // Formulario para editar permisos de un formulario en un perfil
    @GetMapping("/{perfilId}/editar/{formularioId}")
    public String mostrarFormularioEditar(
            @PathVariable Long perfilId,
            @PathVariable Long formularioId,
            Model model) {

        Optional<Perfil> perfil = perfilRepository.findById(perfilId);
        Optional<Formulario> formulario = formularioRepository.findById(formularioId);

        if (!perfil.isPresent() || !formulario.isPresent()) {
            return "redirect:/formularios-x-perfiles";
        }

        FormularioXPerfilId id = new FormularioXPerfilId();
        id.setPerfilId(perfilId);
        id.setFormularioId(formularioId);

        Optional<FormularioXPerfil> asignacion = formularioXPerfilRepository.findById(id);

        if (!asignacion.isPresent()) {
            return "redirect:/formularios-x-perfiles/" + perfilId;
        }

        model.addAttribute("perfil", perfil.get());
        model.addAttribute("formulario", formulario.get());
        model.addAttribute("asignacion", asignacion.get());

        return "formularios-x-perfiles/editar-formulario";
    }

    // Actualizar permisos de formulario en perfil
    @PostMapping("/{perfilId}/editar/{formularioId}")
    public String actualizarFormulario(
            @PathVariable Long perfilId,
            @PathVariable Long formularioId,
            @RequestParam char crear,
            @RequestParam char editar,
            @RequestParam char visualizar,
            @RequestParam char eliminar,
            RedirectAttributes ra) {

        FormularioXPerfilId id = new FormularioXPerfilId();
        id.setPerfilId(perfilId);
        id.setFormularioId(formularioId);

        Optional<FormularioXPerfil> asignacion = formularioXPerfilRepository.findById(id);

        if (!asignacion.isPresent()) {
            ra.addFlashAttribute("error", "Asignación no encontrada");
            return "redirect:/formularios-x-perfiles";
        }

        FormularioXPerfil fxp = asignacion.get();
        fxp.setCrear(crear);
        fxp.setEditar(editar);
        fxp.setVisualizar(visualizar);
        fxp.setEliminar(eliminar);

        formularioXPerfilRepository.save(fxp);
        ra.addFlashAttribute("mensajeExito", "Permisos actualizados correctamente");

        return "redirect:/formularios-x-perfiles/" + perfilId;
    }

    // Eliminar asignación de formulario a perfil
    @PostMapping("/{perfilId}/eliminar/{formularioId}")
    public String eliminarAsignacion(
            @PathVariable Long perfilId,
            @PathVariable Long formularioId,
            RedirectAttributes ra) {

        FormularioXPerfilId id = new FormularioXPerfilId();
        id.setPerfilId(perfilId);
        id.setFormularioId(formularioId);

        Optional<FormularioXPerfil> asignacion = formularioXPerfilRepository.findById(id);

        if (!asignacion.isPresent()) {
            ra.addFlashAttribute("error", "Asignación no encontrada");
            return "redirect:/formularios-x-perfiles";
        }

        formularioXPerfilRepository.deleteById(id);
        ra.addFlashAttribute("mensajeExito", "Formulario desasignado del perfil");

        return "redirect:/formularios-x-perfiles/" + perfilId;
    }
}
