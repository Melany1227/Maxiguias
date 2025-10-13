package com.maxiguias.maxigestion.maxigestion.controlador;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maxiguias.maxigestion.maxigestion.dto.CiudadDTO;
import com.maxiguias.maxigestion.maxigestion.modelo.Ciudad;
import com.maxiguias.maxigestion.maxigestion.repositorio.CiudadRepository;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private CiudadRepository ciudadRepository;

    @GetMapping("/ciudades")
    public ResponseEntity<List<CiudadDTO>> obtenerTodasLasCiudades() {
        List<Ciudad> ciudades = ciudadRepository.findAll();
        List<CiudadDTO> ciudadesDTO = ciudades.stream()
                .map(ciudad -> new CiudadDTO(ciudad.getId(), ciudad.getNombre()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ciudadesDTO);
    }

    @GetMapping("/departamentos/{departamentoId}/ciudades")
    public ResponseEntity<List<CiudadDTO>> obtenerCiudadesPorDepartamento(@PathVariable Integer departamentoId) {
        try {
            System.out.println("Buscando ciudades para departamento: " + departamentoId);
            List<Ciudad> ciudades = ciudadRepository.findByDepartamento_Id(departamentoId);
            System.out.println("Ciudades encontradas: " + ciudades.size());
            
            List<CiudadDTO> ciudadesDTO = ciudades.stream()
                    .map(ciudad -> {
                        System.out.println("Procesando ciudad: " + ciudad.getNombre() + " - ID: " + ciudad.getId());
                        return new CiudadDTO(ciudad.getId(), ciudad.getNombre());
                    })
                    .collect(Collectors.toList());
            
            System.out.println("DTOs creados: " + ciudadesDTO.size());
            return ResponseEntity.ok(ciudadesDTO);
        } catch (Exception e) {
            System.err.println("Error obteniendo ciudades: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}