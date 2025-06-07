package com.maxiguias.maxigestion.maxigestion.modelo;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class PersonaInfo {

    private String nombre;
    private String direccion;
    private String telefono; 

    public PersonaInfo(String nombre, String direccion, String telefono) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
    }

}
