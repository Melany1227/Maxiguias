package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.maxiguias.maxigestion.maxigestion.modelo.Terminado;
import com.maxiguias.maxigestion.maxigestion.servicio.TerminadoService;

@Controller
@RequestMapping("/terminados")
public class TerminadoController {

    private final TerminadoService terminadoService;

    public TerminadoController(TerminadoService terminadoService) {
        this.terminadoService = terminadoService;
    }

    @GetMapping("")
    public String listarTerminados() {
        return "listar_terminados"; 
    }

    @GetMapping("/por-producto/{id}")
    @ResponseBody
    public List<Terminado> obtenerTerminadosPorProducto(@PathVariable("id") Long productoId) {
        return terminadoService.obtenerTerminadosPorProducto(productoId);
    }


}
