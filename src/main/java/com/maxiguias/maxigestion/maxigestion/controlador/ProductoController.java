package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.maxiguias.maxigestion.maxigestion.modelo.Producto;
import com.maxiguias.maxigestion.maxigestion.servicio.ProductoService;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
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
            BindingResult binding,
            Model model,
            RedirectAttributes ra) {

        try {
            productoService.guardarProducto(producto);
            ra.addFlashAttribute("mensaje", "Producto creado exitosamente");
            return "redirect:/productos";

        } catch (IllegalArgumentException e) {
            // ra.addFlashAttribute("error", "No se puede crear el producto ya existe");
            model.addAttribute("producto", producto);
            model.addAttribute("error", "El código ingresado ya existe");
            return "productos/nuevoProducto";

        } catch (Exception e) {
            // Otros errores inesperados
            ra.addFlashAttribute("error", "Ocurrió un error al crear el producto");
            ra.addFlashAttribute("error", e.getMessage());
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
    public String actualizarProducto(@PathVariable Long id, @ModelAttribute Producto producto,
            RedirectAttributes ra) {
        try {
            productoService.actualizarProducto(id, producto);
            ra.addFlashAttribute("Mensaje", "Producto actualizado exitosamente");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al actualizar el producto");
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

    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        List<Producto> productos = productoService.listarProductos();
        model.addAttribute("productos", productos);
        return "productos/catalogo"; // nueva vista
    }

}