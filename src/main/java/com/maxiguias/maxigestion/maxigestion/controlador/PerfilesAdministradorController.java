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

import com.maxiguias.maxigestion.maxigestion.modelo.Perfil;
import com.maxiguias.maxigestion.maxigestion.modelo.Rol;
import com.maxiguias.maxigestion.maxigestion.repositorio.PerfilRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.RolRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.FormularioXPerfilRepository;

@Controller
@RequestMapping("/perfiles")
public class PerfilesAdministradorController {

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FormularioXPerfilRepository formularioXPerfilRepository;

    // Listar todos los perfiles
    @GetMapping
    public String listarPerfiles(Model model) {
        List<Perfil> perfiles = perfilRepository.findAll();
        model.addAttribute("perfiles", perfiles);
        return "perfiles/listar-perfiles";
    }

    // Formulario para crear nuevo perfil
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("roles", roles);
        return "perfiles/crear-perfil";
    }

    // Guardar nuevo perfil
    @PostMapping("/nuevo")
    public String crearPerfil(
            @RequestParam String nombrePerfil,
            @RequestParam Long rolId,
            RedirectAttributes ra) {

        // Validar que el nombre sea único
        List<Perfil> perfilesExistentes = perfilRepository.findAll();
        boolean nombreExiste = perfilesExistentes.stream()
                .anyMatch(p -> p.getNombrePerfil().equalsIgnoreCase(nombrePerfil));

        if (nombreExiste) {
            ra.addFlashAttribute("error", "Ya existe un perfil con este nombre");
            return "redirect:/perfiles/nuevo";
        }

        // Validar que el rol exista
        Optional<Rol> rol = rolRepository.findById(rolId);
        if (!rol.isPresent()) {
            ra.addFlashAttribute("error", "El rol seleccionado no existe");
            return "redirect:/perfiles/nuevo";
        }

        Perfil perfil = new Perfil();
        perfil.setNombrePerfil(nombrePerfil.trim());
        perfil.setRol(rol.get());

        perfilRepository.save(perfil);
        ra.addFlashAttribute("mensajeExito", "Perfil creado correctamente");

        return "redirect:/perfiles";
    }

    // Formulario para editar perfil
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            Model model) {

        Optional<Perfil> perfil = perfilRepository.findById(id);

        if (!perfil.isPresent()) {
            return "redirect:/perfiles";
        }

        List<Rol> roles = rolRepository.findAll();
        model.addAttribute("perfil", perfil.get());
        model.addAttribute("roles", roles);

        return "perfiles/editar-perfil";
    }

    // Actualizar perfil
    @PostMapping("/editar/{id}")
    public String actualizarPerfil(
            @PathVariable Long id,
            @RequestParam String nombrePerfil,
            @RequestParam Long rolId,
            RedirectAttributes ra) {

        Optional<Perfil> perfilOptional = perfilRepository.findById(id);

        if (!perfilOptional.isPresent()) {
            ra.addFlashAttribute("error", "Perfil no encontrado");
            return "redirect:/perfiles";
        }

        // Validar nombre único (excepto el perfil actual)
        List<Perfil> perfilesExistentes = perfilRepository.findAll();
        boolean nombreExiste = perfilesExistentes.stream()
                .anyMatch(p -> p.getNombrePerfil().equalsIgnoreCase(nombrePerfil) &&
                        !p.getId().equals(id));

        if (nombreExiste) {
            ra.addFlashAttribute("error", "Ya existe otro perfil con este nombre");
            return "redirect:/perfiles/editar/" + id;
        }

        // Validar que el rol exista
        Optional<Rol> rol = rolRepository.findById(rolId);
        if (!rol.isPresent()) {
            ra.addFlashAttribute("error", "El rol seleccionado no existe");
            return "redirect:/perfiles/editar/" + id;
        }

        Perfil perfil = perfilOptional.get();
        perfil.setNombrePerfil(nombrePerfil.trim());
        perfil.setRol(rol.get());

        perfilRepository.save(perfil);
        ra.addFlashAttribute("mensajeExito", "Perfil actualizado correctamente");

        return "redirect:/perfiles";
    }

    // Eliminar perfil
    @PostMapping("/eliminar/{id}")
    public String eliminarPerfil(
            @PathVariable Long id,
            RedirectAttributes ra) {

        Optional<Perfil> perfil = perfilRepository.findById(id);

        if (!perfil.isPresent()) {
            ra.addFlashAttribute("error", "Perfil no encontrado");
            return "redirect:/perfiles";
        }

        // Verificar si hay usuarios con este perfil
        long usuariosConPerfil = usuarioRepository.countByPerfil_Id(id);
        if (usuariosConPerfil > 0) {
            ra.addFlashAttribute("error", "No se puede eliminar este perfil porque hay " + usuariosConPerfil + " usuario(s) asignado(s)");
            return "redirect:/perfiles";
        }

        // Eliminar todas las asignaciones formulario_x_perfil relacionadas
        formularioXPerfilRepository.deleteByPerfilId(id);

        // Eliminar el perfil
        perfilRepository.deleteById(id);

        ra.addFlashAttribute("mensajeExito", "Perfil eliminado correctamente");

        return "redirect:/perfiles";
    }
}
