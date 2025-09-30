package com.maxiguias.maxigestion.maxigestion.controlador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.Orden;
import com.maxiguias.maxigestion.maxigestion.modelo.Producto;
import com.maxiguias.maxigestion.maxigestion.modelo.Terminado;
import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.CiudadRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.EmpresaRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.ProductoRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.TerminadoRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;
import com.maxiguias.maxigestion.maxigestion.servicio.OrdenService;
import com.maxiguias.maxigestion.maxigestion.servicio.TerminadoService;

@Controller
@RequestMapping("/ordenes")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    @Autowired
    private TerminadoService terminadoService;

    @Autowired
    private TerminadoRepository terminadoRepository;

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("orden", new Orden());
        model.addAttribute("detalles", new ArrayList<DetalleOrden>());
        model.addAttribute("empresa", empresaRepository.findAll().get(0)); 
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("ciudades", ciudadRepository.findAll());
        model.addAttribute("terminados", terminadoService.obtenerTodosLosTerminados()); 

        return "orden-form";
    }

    @GetMapping("/buscar-usuarios")
    @ResponseBody
    public List<Usuario> buscarUsuarios(@RequestParam String termino) {
        List<Usuario> usuarios = usuarioRepository.findByTipoUsuario_NombreIn(Arrays.asList("NATURAL", "JURIDICO"));
        
        return usuarios.stream()
            .filter(usuario -> {
                String nombre = usuario.getNombre() != null ? usuario.getNombre() : "";
                String primerApellido = usuario.getPrimerApellido() != null ? usuario.getPrimerApellido() : "";
                String nombreCompleto = (nombre + " " + primerApellido).toLowerCase();
                String terminoBusqueda = termino.toLowerCase();
                
                return nombreCompleto.contains(terminoBusqueda) ||
                       nombre.toLowerCase().contains(terminoBusqueda) ||
                       primerApellido.toLowerCase().contains(terminoBusqueda) ||
                       usuario.getDocumento().toString().contains(termino);
            })
            .collect(Collectors.toList());
    }


    @PostMapping("/guardar")
    public String guardarOrden(
            @ModelAttribute Orden orden,
            @RequestParam("productoId") List<Long> productosId,
            @RequestParam("terminadoId") List<Long> terminadosId,
            @RequestParam("cantidad") List<Integer> cantidades,
            @RequestParam("valor") List<BigDecimal> valores,
            @RequestParam("descripcion") List<String> descripciones
    ) {

        Orden ordenGuardada = ordenService.guardarOrden(orden);

        for (int i = 0; i < productosId.size(); i++) {
            DetalleOrden detalle = new DetalleOrden();
            Producto producto = productoRepository.findById(productosId.get(i))
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            detalle.setOrden(ordenGuardada); 
            detalle.setProducto(producto);       
            detalle.setCantidad(cantidades.get(i));
            detalle.setValor(valores.get(i));
            detalle.setDescripcion(descripciones.get(i));
            ordenService.guardarDetalles(detalle);
        }

        return "redirect:/ordenes";
    }

    @GetMapping
    public String listarOrdenes(Model model) {
        List<Orden> ordenes = ordenService.obtenerTodasLasOrdenes();
        model.addAttribute("ordenes", ordenes);
        return "listar_ordenes"; 
    }

    @GetMapping("/verMas/{id}")
    public String verDetalleOrden(@PathVariable("id") Long id, Model model) {
        Orden orden = ordenService.obtenerOrdenPorId(id);
        if (orden == null) {
            return "redirect:/ordenes";
        }

        model.addAttribute("orden", orden);
        model.addAttribute("detalles", orden.getDetalles());
        return "orden-detalle";
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> descargarOrdenPDF(@PathVariable("id") Long id) {
        try {
            byte[] pdfBytes = ordenService.generarOrdenPDF(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "orden_" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}