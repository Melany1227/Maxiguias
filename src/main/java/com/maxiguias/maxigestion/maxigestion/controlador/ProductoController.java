package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.maxiguias.maxigestion.maxigestion.modelo.Producto;
import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.servicio.AutorizacionService;
import com.maxiguias.maxigestion.maxigestion.servicio.CloudinaryService;
import com.maxiguias.maxigestion.maxigestion.servicio.ProductoService;

import jakarta.servlet.http.HttpSession;

@Controller

@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private AutorizacionService AutorizacionService;

    private final ProductoService productoService;
    private final CloudinaryService cloudinaryService;

    public ProductoController(ProductoService productoService, CloudinaryService cloudinaryService) {
        this.productoService = productoService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public String listarProductos(@RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
        List<Producto> productos;

        if (keyword != null && !keyword.isEmpty()) {
            productos = productoService.buscarPorCodigoONombre(keyword);
        } else {
            productos = productoService.listarProductos();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("keyword", keyword);

        return "productos/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/nuevoProducto";
    }

    @PostMapping("/nuevo")
    public String crearProducto(
            @ModelAttribute("producto") Producto producto,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            BindingResult binding,
            Model model,
            RedirectAttributes ra) {

        System.out.println("=== INICIANDO CREACIÓN DE PRODUCTO ===");
        System.out.println("ID: " + producto.getId());
        System.out.println("Nombre: " + producto.getNombre());
        System.out.println(
                "Terminados: " + (producto.getTerminados() != null ? producto.getTerminados().size() : "null"));

        try {
            // Subir imagen si se proporciona
            if (imagenFile != null && !imagenFile.isEmpty()) {
                System.out.println("=== SUBIENDO IMAGEN ===");
                System.out.println("Archivo: " + imagenFile.getOriginalFilename());
                System.out.println("Tamaño: " + imagenFile.getSize());

                String publicId = cloudinaryService.uploadImage(imagenFile);
                System.out.println("Public ID generado: " + publicId);
                producto.setImagen(publicId);
            } else {
                System.out.println("=== NO HAY ARCHIVO DE IMAGEN ===");
            }

            System.out.println("URL final del producto: " + producto.getImagen());
            productoService.guardarProducto(producto);
            ra.addFlashAttribute("mensaje", "Producto creado exitosamente");
            return "redirect:/productos";

        } catch (IllegalArgumentException e) {
            System.out.println("=== ERROR: CÓDIGO DUPLICADO ===");
            System.out.println("Error: " + e.getMessage());
            model.addAttribute("producto", producto);
            model.addAttribute("error", "El código ingresado ya existe");
            return "productos/nuevoProducto";

        } catch (Exception e) {
            System.out.println("=== ERROR GENERAL ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("error", "Ocurrió un error al crear el producto: " + e.getMessage());
            return "redirect:/productos";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Optional<Producto> producto = productoService.obtenerProductoPorId(id);
        if (producto.isPresent()) {
            model.addAttribute("producto", producto.get());
            return "productos/nuevoProducto";
        } else {
            return "redirect:/productos";
        }
    }

    @PostMapping("/{id}")
    public String actualizarProducto(
            @PathVariable Long id,
            @ModelAttribute Producto producto,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes ra) {
        try {
            // Obtener producto existente para mantener imagen actual si no se sube nueva
            Optional<Producto> productoExistente = productoService.obtenerProductoPorId(id);

            if (imagenFile != null && !imagenFile.isEmpty()) {
                // Eliminar imagen anterior si existe
                if (productoExistente.isPresent() && productoExistente.get().getImagen() != null) {
                    cloudinaryService.deleteImage(productoExistente.get().getImagen());
                }
                // Subir nueva imagen
                String publicId = cloudinaryService.uploadImage(imagenFile);
                producto.setImagen(publicId);
            } else if (productoExistente.isPresent()) {
                // Mantener imagen existente
                producto.setImagen(productoExistente.get().getImagen());
            }

            productoService.actualizarProducto(id, producto);
            ra.addFlashAttribute("mensaje", "Producto actualizado exitosamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al actualizar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    @DeleteMapping("/{id}/eliminar")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.ok("Producto eliminado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al eliminar el producto.");
        }
    }

    // @GetMapping("/catalogo")
    // public String catalogo(Model model, HttpSession session) {
    // List<Producto> productos = productoService.listarProductos();
    // model.addAttribute("productos", productos);
    // model.addAttribute("cloudinaryService", cloudinaryService);

    // String rolUsuario = "PUBLICO"; // valor por defecto

    // Recuperar usuario guardado en sesión
    // Usuario usuario = (Usuario) session.getAttribute("usuario");
    // if (usuario != null && usuario.getPerfil() != null &&
    // usuario.getPerfil().getRol() != null) {
    // String nombreRol = usuario.getPerfil().getRol().getNombreRol();
    // if ("ADMINISTRADOR".equalsIgnoreCase(nombreRol)) {
    // rolUsuario = "ADMINISTRADOR";
    // } else if ("JURIDICO".equalsIgnoreCase(nombreRol)) {
    // rolUsuario = "JURIDICO";
    // }
    // }

    // System.out.println("=== Rol del usuario detectado: " + rolUsuario);
    // model.addAttribute("rolUsuario", rolUsuario);

    // return "productos/catalogo";
    // }

    // === Catálogo público ===
    // @GetMapping("/publico")
    // public String catalogoPublico(Model model) {
    // List<Producto> productos = productoService.listarProductos();
    // model.addAttribute("productos", productos);
    // model.addAttribute("cloudinaryService", cloudinaryService);
    // model.addAttribute("rolUsuario", "NATURAL");
    // return "productos/catalogo";
    // }

    // === Catálogo jurídico ===
    // @GetMapping("/juridico")
    // public String catalogoJuridico(Model model, HttpSession session) {
    // Usuario usuario = (Usuario) session.getAttribute("usuario");
    // String rolUsuario = "NATURAL";

    // if (usuario != null && usuario.getPerfil() != null &&
    // usuario.getPerfil().getRol() != null) {
    // String nombreRol = usuario.getPerfil().getRol().getNombreRol();
    // if ("JURIDICO".equalsIgnoreCase(nombreRol)) {
    // rolUsuario = "JURIDICO";
    // }
    // }

    // if (!"JURIDICO".equals(rolUsuario)) {
    // return "redirect:/error/403";
    // }
    //
    // List<Producto> productos = productoService.listarProductos();
    // model.addAttribute("productos", productos);
    // model.addAttribute("cloudinaryService", cloudinaryService);
    // model.addAttribute("rolUsuario", rolUsuario);
    // return "productos/catalogo";
    // }

    @GetMapping("/publico")
    public String catalogoPublico(Model model) {
        List<Producto> productos = productoService.listarProductos();
        model.addAttribute("productos", productos);
        model.addAttribute("cloudinaryService", cloudinaryService);
        model.addAttribute("rolUsuario", "NATURAL");
        return "productos/catalogo";
    }

    // === Catálogo jurídico ===
    @GetMapping("/juridico")
    public String catalogoJuridico(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Verificar permiso utilizando tu servicio centralizado
        boolean tienePermiso = AutorizacionService.tienePermiso(usuario, "/productos/juridico", "VISUALIZAR");

        if (!tienePermiso) {
            return "redirect:/error/403";
        }

        List<Producto> productos = productoService.listarProductos();
        model.addAttribute("productos", productos);
        model.addAttribute("cloudinaryService", cloudinaryService);
        model.addAttribute("rolUsuario", usuario.getPerfil().getRol().getNombreRol());

        return "productos/catalogo";
    }

}
