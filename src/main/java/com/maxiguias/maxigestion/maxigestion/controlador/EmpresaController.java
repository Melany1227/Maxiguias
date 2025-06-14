package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.maxiguias.maxigestion.maxigestion.modelo.Empresa;
import com.maxiguias.maxigestion.maxigestion.servicio.EmpresaService;

@Controller
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping()
    public String listarEmpresas(Model model) {
        List<Empresa> empresas = empresaService.listarEmpresas();
        model.addAttribute("empresas", empresas);
        return "listar_empresas"; 
    }
}