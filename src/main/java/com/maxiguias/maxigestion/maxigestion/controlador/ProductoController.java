package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    public String listarProductos(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        try {
            Page<Producto> productosPage;

            // Buscar por keyword si está presente
            if (keyword != null && !keyword.trim().isEmpty()) {
                productosPage = productoService.buscarPorCodigoONombrePaginado(keyword.trim(), page, size);
            } else {
                productosPage = productoService.listarProductosPaginados(page, size);
            }

            List<Producto> productos = productosPage.getContent();

            // Si no hay productos, mostrar mensaje
            if (productos.isEmpty()) {
                model.addAttribute("mensajeNoResultados", "No se encontraron productos que coincidan con la búsqueda.");
            }

            model.addAttribute("productos", productos);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productosPage.getTotalPages());
            model.addAttribute("totalElements", productosPage.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("keyword", keyword);

        } catch (Exception e) {
            // Registrar el error y mostrar mensaje amigable
            System.err.println("Error al listar productos: " + e.getMessage());
            model.addAttribute("errorMensaje",
                    "Ocurrió un error al cargar los productos. Por favor, intenta nuevamente.");
        }

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
            @RequestParam(value = "imagenCloudinary", required = false) String imagenCloudinary,
            BindingResult binding,
            Model model,
            RedirectAttributes ra) {

        System.out.println("=== INICIANDO CREACIÓN DE PRODUCTO ===");
        System.out.println("ID: " + producto.getId());
        System.out.println("Nombre: " + producto.getNombre());

        try {
            // Tomar imagen desde Cloudinary si se proporcionó texto, si no, subir archivo si existe
            if (imagenCloudinary != null && !imagenCloudinary.trim().isEmpty()) {
                producto.setImagen(imagenCloudinary.trim());
            } else if (imagenFile != null && !imagenFile.isEmpty()) {
                System.out.println("=== SUBIENDO IMAGEN ===");
                String publicId = cloudinaryService.uploadImage(imagenFile);
                producto.setImagen(publicId);
            }

            productoService.guardarProducto(producto);
            ra.addFlashAttribute("mensaje", "Producto creado exitosamente");
            return "redirect:/productos";

        } catch (IllegalArgumentException e) {
            // anejo de errores de validación
            System.out.println("=== ERROR DE VALIDACIÓN ===");
            System.out.println(e.getMessage());

            producto.setId(null); // evita confusión con edición
            model.addAttribute("producto", producto);
            model.addAttribute("errorCodigo", e.getMessage());

            return "productos/nuevoProducto"; // recarga formulario con error
        } catch (Exception e) {
            // Errores generales
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
            @RequestParam(value = "imagenCloudinary", required = false) String imagenCloudinary,
            RedirectAttributes ra) {
        try {
            // Obtener producto existente para mantener imagen actual si no se sube nueva
            Optional<Producto> productoExistente = productoService.obtenerProductoPorId(id);

            if (imagenCloudinary != null && !imagenCloudinary.trim().isEmpty()) {
                if (productoExistente.isPresent() && productoExistente.get().getImagen() != null
                        && !imagenCloudinary.trim().equals(productoExistente.get().getImagen())) {
                    cloudinaryService.deleteImage(productoExistente.get().getImagen());
                }
                producto.setImagen(imagenCloudinary.trim());
            } else if (imagenFile != null && !imagenFile.isEmpty()) {
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
            ra.addFlashAttribute("mensajeEdicion", "Producto actualizado satisfactoriamente");
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

    // === Catálogo público ===
    @GetMapping("/publico")
    public String catalogoPublico(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        // Servicio con paginación
        var productosPage = productoService.listarProductosPaginados(page, size);

        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productosPage.getTotalPages());
        model.addAttribute("totalElements", productosPage.getTotalElements());
        model.addAttribute("size", size);

        model.addAttribute("cloudinaryService", cloudinaryService);
        model.addAttribute("rolUsuario", "NATURAL");

        return "productos/catalogo";
    }

    // === Catálogo jurídico ===
    @GetMapping("/juridico")
    public String catalogoJuridico(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        boolean tienePermiso = AutorizacionService.tienePermiso(usuario, "/productos/juridico", "VISUALIZAR");
        if (!tienePermiso) {
            return "redirect:/error/403";
        }

        // Servicio con paginación
        var productosPage = productoService.listarProductosPaginados(page, size);

        model.addAttribute("productos", productosPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productosPage.getTotalPages());
        model.addAttribute("totalElements", productosPage.getTotalElements());
        model.addAttribute("size", size);

        model.addAttribute("cloudinaryService", cloudinaryService);
        model.addAttribute("rolUsuario", usuario.getPerfil().getRol().getNombreRol());

        return "productos/catalogo";
    }

}
