package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.CiudadRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.DepartamentoRepository;
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

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    @GetMapping()
    public String listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String buscar,
            Model model) {
        
        // Ordenamiento simple por documento ascendente
        Pageable pageable = PageRequest.of(page, size, Sort.by("documento").ascending());
        Page<Usuario> usuariosPage;
        
        // Verificar si hay término de búsqueda
        if (buscar != null && !buscar.trim().isEmpty()) {
            usuariosPage = usuarioService.buscarUsuariosPaginados(buscar.trim(), pageable);
        } else {
            usuariosPage = usuarioService.obtenerUsuariosPaginados(pageable);
        }
        
        model.addAttribute("usuarios", usuariosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosPage.getTotalPages());
        model.addAttribute("totalElements", usuariosPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("buscar", buscar);
        
        return "listar_usuarios"; 
    }

    @GetMapping("/crear")
    public String mostrarFormularioCrearUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("tiposUsuario", tipoUsuarioService.obtenerTiposUsuario()); 
        model.addAttribute("perfiles", perfilService.obtenerPerfiles());
        model.addAttribute("departamentos", departamentoRepository.findAll());
        model.addAttribute("ciudades", ciudadRepository.findAll()); 
        model.addAttribute("esEdicion", false);
        model.addAttribute("tieneContrasena", false);
        return "crear_usuario";
    }

    @PostMapping()
    public String crearUsuario(@ModelAttribute Usuario usuario, 
                              @RequestParam(required = false) String confirmarContrasena, 
                              Model model) {
        
        // Validar confirmación de contraseña si se proporcionó
        if (usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty() &&
            confirmarContrasena != null && !confirmarContrasena.trim().isEmpty()) {
            if (!usuario.getContrasena().equals(confirmarContrasena)) {
                model.addAttribute("usuario", usuario);
                model.addAttribute("mensajeModal", "Las contraseñas no coinciden.");
                model.addAttribute("tipoMensaje", "error");
                model.addAttribute("tiposUsuario", tipoUsuarioService.obtenerTiposUsuario()); 
                model.addAttribute("perfiles", perfilService.obtenerPerfiles());
                model.addAttribute("departamentos", departamentoRepository.findAll());
                model.addAttribute("ciudades", ciudadRepository.findAll()); 
                model.addAttribute("esEdicion", false);
                model.addAttribute("tieneContrasena", false);
                return "crear_usuario";
            }
        }
        
        String resultado = usuarioService.crearUsuario(usuario);
        model.addAttribute("tiposUsuario", tipoUsuarioService.obtenerTiposUsuario()); 
        model.addAttribute("perfiles", perfilService.obtenerPerfiles());
        model.addAttribute("departamentos", departamentoRepository.findAll());
        model.addAttribute("ciudades", ciudadRepository.findAll()); 
        model.addAttribute("esEdicion", false);

        if (!resultado.equals("Usuario guardado exitosamente.")) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("mensajeModal", resultado);
            model.addAttribute("tipoMensaje", "error");
            model.addAttribute("tieneContrasena", false);
            return "crear_usuario";
        }

        model.addAttribute("mensajeModal", resultado);
        model.addAttribute("tipoMensaje", "success");
        model.addAttribute("redirigirDespues", true);
        model.addAttribute("tieneContrasena", false);
        return "crear_usuario";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id); 
        
        // Guardar si tiene contraseña para el placeholder, luego limpiar el campo
        boolean tieneContrasena = usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty();
        if (tieneContrasena) {
            usuario.setContrasena(""); // Limpiar para que no se muestre la encriptada
        }
        
        model.addAttribute("tiposUsuario", tipoUsuarioService.obtenerTiposUsuario()); 
        model.addAttribute("perfiles", perfilService.obtenerPerfiles());
        model.addAttribute("departamentos", departamentoRepository.findAll());
        model.addAttribute("ciudades", ciudadRepository.findAll()); 
        model.addAttribute("usuario", usuario);
        model.addAttribute("esEdicion", true);
        model.addAttribute("tieneContrasena", tieneContrasena);
        return "crear_usuario"; 
    }

    @PostMapping("/actualizar")
    public String actualizarUsuario(@ModelAttribute Usuario usuario, 
                                   @RequestParam(required = false) String confirmarContrasena, 
                                   Model model) {
        
        // Validar confirmación de contraseña si se proporcionó una nueva contraseña
        if (usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty() &&
            confirmarContrasena != null && !confirmarContrasena.trim().isEmpty()) {
            if (!usuario.getContrasena().equals(confirmarContrasena)) {
                model.addAttribute("mensajeModal", "Las contraseñas no coinciden."); 
                model.addAttribute("tipoMensaje", "error");
                model.addAttribute("usuario", usuario);
                model.addAttribute("tiposUsuario", tipoUsuarioService.obtenerTiposUsuario());
                model.addAttribute("perfiles", perfilService.obtenerPerfiles());
                model.addAttribute("departamentos", departamentoRepository.findAll());
                model.addAttribute("ciudades", ciudadRepository.findAll());
                model.addAttribute("esEdicion", true);
                model.addAttribute("tieneContrasena", true);
                return "crear_usuario";
            }
        }
        
        String resultado = usuarioService.actualizarUsuario(usuario);

        if (!resultado.equals("Usuario actualizado exitosamente.")) {
            model.addAttribute("mensajeModal", resultado); 
            model.addAttribute("tipoMensaje", "error");
            model.addAttribute("usuario", usuario);
            model.addAttribute("tiposUsuario", tipoUsuarioService.obtenerTiposUsuario());
            model.addAttribute("perfiles", perfilService.obtenerPerfiles());
            model.addAttribute("departamentos", departamentoRepository.findAll());
            model.addAttribute("ciudades", ciudadRepository.findAll());
            model.addAttribute("esEdicion", true);
            model.addAttribute("tieneContrasena", true); // Asumir que tiene contraseña en edición
            return "crear_usuario";
        }

        model.addAttribute("mensajeModal", resultado);
        model.addAttribute("tipoMensaje", "success");
        model.addAttribute("redirigirDespues", true); 
        model.addAttribute("esEdicion", true);
        model.addAttribute("tieneContrasena", true);
        return "crear_usuario";
    }


    @GetMapping("/ver/{id}")
    public String verDetallesUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.obtenerPorId(id);
        
        if (usuario == null) {
            model.addAttribute("mensajeModal", "Usuario no encontrado.");
            model.addAttribute("tipoMensaje", "error");
            return "redirect:/usuarios";
        }
        
        model.addAttribute("usuario", usuario);
        return "ver_usuario";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(RedirectAttributes redirectAttributes, @PathVariable Long id) {
        usuarioService.eliminarPorId(id);
        redirectAttributes.addFlashAttribute("mensajeModal", "Usuario eliminado exitosamente.");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        redirectAttributes.addFlashAttribute("redirigirDespues", true);
        return "redirect:/usuarios";
    }


}
