package com.maxiguias.maxigestion.maxigestion.controlador;

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleFactura;
import com.maxiguias.maxigestion.maxigestion.modelo.Factura;
import com.maxiguias.maxigestion.maxigestion.modelo.Producto;
import com.maxiguias.maxigestion.maxigestion.repositorio.*;
import com.maxiguias.maxigestion.maxigestion.servicio.FacturaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("factura", new Factura());
        model.addAttribute("detalles", new ArrayList<DetalleFactura>());

        model.addAttribute("usuarios", usuarioRepository.findAll());
        model.addAttribute("empresas", empresaRepository.findAll());
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("ciudades", ciudadRepository.findAll());

        return "factura-form";
    }

    @PostMapping("/guardar")
    public String guardarFactura(@ModelAttribute Factura factura,
                                 @RequestParam("productoId") List<Long> productoIds,
                                 @RequestParam("cantidad") List<Integer> cantidades,
                                 @RequestParam("valor") List<BigDecimal> valores,
                                 @RequestParam("descripcion") List<String> descripciones) {

        List<DetalleFactura> detalles = new ArrayList<>();

        for (int i = 0; i < productoIds.size(); i++) {
            DetalleFactura detalle = new DetalleFactura();
            Producto producto = new Producto();
            producto.setId(productoIds.get(i));
            detalle.setProducto(producto);
            detalle.setCantidad(cantidades.get(i));
            detalle.setValor(valores.get(i));
            detalle.setDescripcion(descripciones.get(i));
            detalles.add(detalle);
        }

        facturaService.crearFactura(factura, detalles);

        return "redirect:/facturas/lista";
    }

    @GetMapping("/lista")
    public String listarFacturas(Model model) {
        List<Factura> facturas = facturaRepository.findAll();
        model.addAttribute("facturas", facturas);
        return "factura-lista";
    }

}

