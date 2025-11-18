package com.maxiguias.maxigestion.maxigestion.controlador;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.CiudadRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.DepartamentoRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.PerfilRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.TipoUsuarioRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;
import com.maxiguias.maxigestion.maxigestion.servicio.EmailService;
import com.maxiguias.maxigestion.maxigestion.servicio.PerfilService;
import com.maxiguias.maxigestion.maxigestion.servicio.TipoUsuarioService;
import com.maxiguias.maxigestion.maxigestion.servicio.UsuarioService;

@Controller
public class AuthController {

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

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Usuario o contraseña incorrectos");
        }
        
        if (logout != null) {
            model.addAttribute("logoutMessage", "Se ha cerrado sesión correctamente");
        }
        
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("departamentos", departamentoRepository.findAll());
        model.addAttribute("ciudades", ciudadRepository.findAll());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario, Model model) {
        // Establecer valores por defecto
        usuario.setPrimerApellido(null);
        usuario.setSegundoApellido(null);
        usuario.setTipoUsuario(tipoUsuarioRepository.findByNombre("JURIDICO"));
        usuario.setPerfil(perfilRepository.findByNombrePerfil("ALMACEN"));
        
        String resultado = usuarioService.crearUsuario(usuario);
        
        if (!resultado.equals("Usuario guardado exitosamente.")) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("mensajeModal", resultado);
            model.addAttribute("tipoMensaje", "error");
            model.addAttribute("departamentos", departamentoRepository.findAll());
            model.addAttribute("ciudades", ciudadRepository.findAll());
            return "registro";
        }

        model.addAttribute("mensajeModal", "Registro exitoso. Ya puedes iniciar sesión.");
        model.addAttribute("tipoMensaje", "success");
        model.addAttribute("redirigirLogin", true);
        return "registro";
    }

    @GetMapping("/recuperar-password")
    public String mostrarFormularioRecuperarPassword() {
        return "recuperar-password";
    }

    @PostMapping("/recuperar-password")
    public String procesarRecuperarPassword(@RequestParam String correo, Model model) {
        try {
            String resultado = usuarioService.restablecerContrasena(correo);
            
            if (resultado.startsWith("Error:")) {
                model.addAttribute("mensajeModal", resultado);
                model.addAttribute("tipoMensaje", "error");
            } else {
                // Enviar correo con la nueva contraseña
                emailService.enviarCorreoRecuperacion(correo, resultado);
                // Redirigir directamente al formulario de cambio de contraseña
                return "redirect:/cambiar-password?correo=" + correo + "&mensaje=enviado";
            }
        } catch (Exception e) {
            model.addAttribute("mensajeModal", "Error al procesar la recuperación: " + e.getMessage());
            model.addAttribute("tipoMensaje", "error");
        }
        
        return "recuperar-password";
    }

    @GetMapping("/cambiar-password")
    public String mostrarFormularioCambiarPassword(
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String mensaje,
            Model model) {
        
        // Si viene desde recuperar-password, mostrar mensaje y prellenar correo
        if ("enviado".equals(mensaje) && correo != null) {
            model.addAttribute("mensajeModal", "Se ha enviado una contraseña temporal a tu correo. Revisa tu bandeja de entrada e ingresa la contraseña temporal aquí.");
            model.addAttribute("tipoMensaje", "success");
            model.addAttribute("correoPrelleno", correo);
        }
        
        return "cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String procesarCambiarPassword(
            @RequestParam String correo, 
            @RequestParam String contrasenaTemporal, 
            @RequestParam String nuevaContrasena, 
            @RequestParam String confirmarContrasena, 
            Model model) {
        
        try {
            // Validar que las contraseñas coincidan
            if (!nuevaContrasena.equals(confirmarContrasena)) {
                model.addAttribute("mensajeModal", "Las contraseñas no coinciden.");
                model.addAttribute("tipoMensaje", "error");
                model.addAttribute("correoPrelleno", correo);
                model.addAttribute("contrasenaTemporal", contrasenaTemporal);
                return "cambiar-password";
            }

            // Validar y cambiar contraseña usando el servicio
            String resultado = usuarioService.cambiarContrasenaTemporal(correo, contrasenaTemporal, nuevaContrasena);

            if (resultado.startsWith("Error:")) {
                model.addAttribute("mensajeModal", resultado);
                model.addAttribute("tipoMensaje", "error");
                model.addAttribute("correoPrelleno", correo);
                model.addAttribute("contrasenaTemporal", contrasenaTemporal);
            } else {
                model.addAttribute("mensajeModal", "Contraseña cambiada exitosamente. Ya puedes iniciar sesión.");
                model.addAttribute("tipoMensaje", "success");
                model.addAttribute("redirigirLogin", true);
            }

        } catch (Exception e) {
            model.addAttribute("mensajeModal", "Error al cambiar contraseña: " + e.getMessage());
            model.addAttribute("tipoMensaje", "error");
            model.addAttribute("correoPrelleno", correo);
            model.addAttribute("contrasenaTemporal", contrasenaTemporal);
        }

        return "cambiar-password";
    }
}