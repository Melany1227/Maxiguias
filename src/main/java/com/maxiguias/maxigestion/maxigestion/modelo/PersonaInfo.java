package com.maxiguias.maxigestion.maxigestion.modelo;

import jakarta.persistence.Embeddable;

@Embeddable
public class PersonaInfo {

    private String nombre;
    private String direccion;
    private String telefono; // Using String to avoid numeric overflow

    // Default constructor
    public PersonaInfo() {}

    // Constructor with parameters
    public PersonaInfo(String nombre, String direccion, String telefono) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    // Getters and Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
