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
}
