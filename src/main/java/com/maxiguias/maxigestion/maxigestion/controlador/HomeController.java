package com.maxiguias.maxigestion.maxigestion.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("mensaje", "Hola desde Spring Boot + Thymeleaf + Bootstrap");
        return "index";
    }

    @GetMapping("/ayuda_linea")
    public String ayudaEnLinea() {
        return "ayuda_linea";
    }

    @GetMapping("/acerca_de")
    public String acercaDe() {
        return "acerca_de";
    }
}
