package com.maxiguias.maxigestion.maxigestion.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.DepartamentoRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.CiudadRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String mostrarPerfil(HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");

        if (usuarioLogueado == null) {
            return "redirect:/login";
        }

        Usuario usuarioActual = usuarioRepository.findById(usuarioLogueado.getDocumento())
                .orElse(usuarioLogueado);

        model.addAttribute("usuario", usuarioActual);
        model.addAttribute("departamentos", departamentoRepository.findAll());

        // Si el usuario tiene una ciudad, cargar las ciudades del departamento
        if (usuarioActual.getCiudad() != null && usuarioActual.getCiudad().getDepartamento() != null) {
            model.addAttribute("ciudades", ciudadRepository.findByDepartamento_Id(usuarioActual.getCiudad().getDepartamento().getId()));
        } else {
            model.addAttribute("ciudades", java.util.Collections.emptyList());
        }

        return "perfil";
    }

    @PostMapping
    public String actualizarPerfil(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String primerApellido,
            @RequestParam(required = false) String segundoApellido,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) Integer ciudadId,
            @RequestParam(required = false) String contrasenActual,
            @RequestParam(required = false) String nuevaContrasena,
            @RequestParam(required = false) String confirmarContrasena,
            HttpSession session,
            RedirectAttributes ra) {

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");

        if (usuarioLogueado == null) {
            return "redirect:/login";
        }

        try {
            Usuario usuario = usuarioRepository.findById(usuarioLogueado.getDocumento())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (nombre != null && !nombre.trim().isEmpty()) {
                usuario.setNombre(nombre.trim());
            }
            if (primerApellido != null && !primerApellido.trim().isEmpty()) {
                usuario.setPrimerApellido(primerApellido.trim());
            }
            if (segundoApellido != null && !segundoApellido.trim().isEmpty()) {
                usuario.setSegundoApellido(segundoApellido.trim());
            }
            if (telefono != null && !telefono.trim().isEmpty()) {
                usuario.setTelefono(Long.parseLong(telefono.trim()));
            }
            if (correo != null && !correo.trim().isEmpty()) {
                usuario.setCorreo(correo.trim());
            }
            if (direccion != null && !direccion.trim().isEmpty()) {
                usuario.setDireccion(direccion.trim());
            }
            if (ciudadId != null && ciudadId > 0) {
                usuario.setCiudad(ciudadRepository.findById(ciudadId).orElse(null));
            }

            if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
                if (contrasenActual == null || contrasenActual.isEmpty()) {
                    ra.addFlashAttribute("error", "Debe ingresar su contraseña actual para cambiarla.");
                    return "redirect:/perfil";
                }

                if (!passwordEncoder.matches(contrasenActual, usuario.getContrasena())) {
                    ra.addFlashAttribute("error", "La contraseña actual es incorrecta.");
                    return "redirect:/perfil";
                }

                if (!nuevaContrasena.equals(confirmarContrasena)) {
                    ra.addFlashAttribute("error", "Las nuevas contraseñas no coinciden.");
                    return "redirect:/perfil";
                }

                if (passwordEncoder.matches(nuevaContrasena, usuario.getContrasena())) {
                    ra.addFlashAttribute("error", "La nueva contraseña no puede ser igual a la anterior.");
                    return "redirect:/perfil";
                }

                usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
            }

            usuarioRepository.save(usuario);
            session.setAttribute("usuario", usuario);

            ra.addFlashAttribute("mensajeExito", "Perfil actualizado correctamente.");
        } catch (NumberFormatException e) {
            ra.addFlashAttribute("error", "El teléfono debe ser un número válido.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }

        return "redirect:/perfil";
    }

}
