package com.maxiguias.maxigestion.maxigestion.controlador;

import com.maxiguias.maxigestion.maxigestion.modelo.Empresa;
import com.maxiguias.maxigestion.maxigestion.servicio.EmpresaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping("/empresas")
    public String listarEmpresas(Model model) {
        List<Empresa> empresas = empresaService.listarEmpresas();
        model.addAttribute("empresas", empresas);
        return "listar_empresas";
    }
}