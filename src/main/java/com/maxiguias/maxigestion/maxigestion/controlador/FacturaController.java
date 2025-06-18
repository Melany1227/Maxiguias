package com.maxiguias.maxigestion.maxigestion.controlador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleFactura;
import com.maxiguias.maxigestion.maxigestion.modelo.Factura;
import com.maxiguias.maxigestion.maxigestion.modelo.Producto;
import com.maxiguias.maxigestion.maxigestion.repositorio.CiudadRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.EmpresaRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.ProductoRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;
import com.maxiguias.maxigestion.maxigestion.servicio.FacturaService;
import com.maxiguias.maxigestion.maxigestion.servicio.TerminadoService;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

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

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("factura", new Factura());
        model.addAttribute("detalles", new ArrayList<DetalleFactura>());
        model.addAttribute("usuarios", usuarioRepository.findByTipoUsuario_NombreIn(Arrays.asList("NATURAL", "JURIDICO")));
        model.addAttribute("empresa", empresaRepository.findAll().get(0)); 
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("ciudades", ciudadRepository.findAll());
        model.addAttribute("terminados", terminadoService.obtenerTodosLosTerminados()); 

        return "factura-form";
    }

    @PostMapping("/guardar")
    public String guardarFactura(
            @ModelAttribute Factura factura,
            @RequestParam("productoId") List<Long> productosId,
            @RequestParam("terminadoId") List<Long> terminadosId,
            @RequestParam("cantidad") List<Integer> cantidades,
            @RequestParam("valor") List<BigDecimal> valores,
            @RequestParam("descripcion") List<String> descripciones
    ) {

        Factura facturaGuardada = facturaService.guardarFactura(factura);

        for (int i = 0; i < productosId.size(); i++) {
            DetalleFactura detalle = new DetalleFactura();
            Producto producto = productoRepository.findById(productosId.get(i))
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            detalle.setFactura(facturaGuardada); 
            detalle.setProducto(producto);       
            detalle.setCantidad(cantidades.get(i));
            detalle.setValor(valores.get(i));
            detalle.setDescripcion(descripciones.get(i));
            facturaService.guardarDetalles(detalle);
        }

        return "redirect:/facturas";
    }

    @GetMapping
    public String listarFacturas(Model model) {
        List<Factura> facturas = facturaService.obtenerTodasLasFacturas();
        model.addAttribute("facturas", facturas);
        return "listar_facturas"; 
    }

    @GetMapping("/verMas/{id}")
    public String verDetalleFactura(@PathVariable("id") Long id, Model model) {
        Factura factura = facturaService.obtenerFacturaPorId(id);
        if (factura == null) {
            return "redirect:/facturas"; // Por si no se encuentra
        }

        model.addAttribute("factura", factura);
        model.addAttribute("detalles", factura.getDetalles());
        return "factura-detalle"; // nombre del HTML
    }



    
}

