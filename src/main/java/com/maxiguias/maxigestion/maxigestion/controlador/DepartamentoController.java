package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.maxiguias.maxigestion.maxigestion.modelo.Departamento;
import com.maxiguias.maxigestion.maxigestion.servicio.DepartamentoService;

@Controller
@RequestMapping("/departamentos")
public class DepartamentoController {

    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

    @GetMapping
    public String listarDepartamentos(Model model) {
        List<Departamento> departamentos = departamentoService.listarDepartamentos();
        model.addAttribute("departamentos", departamentos);
        return "departamentos/listar";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("departamento", new Departamento());
        return "departamentos/formulario";
    }

    @GetMapping("/{id}")
    public String obtenerDepartamentoPorId(@PathVariable Integer id, Model model) {
        Optional<Departamento> departamento = departamentoService.obtenerDepartamentoPorId(id);
        if (departamento.isPresent()) {
            model.addAttribute("departamento", departamento.get());
            return "departamentos/detalle";
        } else {
            return "redirect:/departamentos";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Optional<Departamento> departamento = departamentoService.obtenerDepartamentoPorId(id);
        if (departamento.isPresent()) {
            model.addAttribute("departamento", departamento.get());
            return "departamentos/formulario";
        } else {
            return "redirect:/departamentos";
        }
    }

    @PostMapping
    public String crearDepartamento(@ModelAttribute Departamento departamento, RedirectAttributes redirectAttributes) {
        departamentoService.guardarDepartamento(departamento);
        redirectAttributes.addFlashAttribute("mensaje", "Departamento creado exitosamente");
        return "redirect:/departamentos";
    }

    @PostMapping("/{id}")
    public String actualizarDepartamento(@PathVariable Integer id, @ModelAttribute Departamento departamento, RedirectAttributes redirectAttributes) {
        departamento.setId(id);
        departamentoService.guardarDepartamento(departamento);
        redirectAttributes.addFlashAttribute("mensaje", "Departamento actualizado exitosamente");
        return "redirect:/departamentos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarDepartamento(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        departamentoService.eliminarDepartamento(id);
        redirectAttributes.addFlashAttribute("mensaje", "Departamento eliminado exitosamente");
        return "redirect:/departamentos";
    }
}