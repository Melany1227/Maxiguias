package com.maxiguias.maxigestion.maxigestion.servicio;

import com.maxiguias.maxigestion.maxigestion.modelo.Empresa;
import com.maxiguias.maxigestion.maxigestion.modelo.PersonaInfo;
import com.maxiguias.maxigestion.maxigestion.repositorio.EmpresaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public List<Empresa> listarEmpresas() {
        try {
            // Primero intenta con la consulta JPA estándar
            return empresaRepository.findAll();
        } catch (Exception e) {
            // Si falla, usa la consulta nativa
            return convertirObjectArrayAEmpresas(empresaRepository.findAllEmpresasWithObjectData());
        }
    }

    private List<Empresa> convertirObjectArrayAEmpresas(List<Object[]> resultados) {
        List<Empresa> empresas = new ArrayList<>();

        for (Object[] fila : resultados) {
            try {
                System.out.println("Procesando fila: " + java.util.Arrays.toString(fila));
                
                String nitEmpresa = fila[0] != null ? fila[0].toString() : null;
                Date fechaCreacion = (Date) fila[1];
                String nombre = fila[2] != null ? fila[2].toString() : null;
                String direccion = fila[3] != null ? fila[3].toString() : null;
                String telefono = fila[4] != null ? fila[4].toString() : null;

                PersonaInfo infoPersona = new PersonaInfo(nombre, direccion, telefono);
                Empresa empresa = new Empresa(nitEmpresa, fechaCreacion, infoPersona);
                empresas.add(empresa);

                System.out.println("Empresa creada: " + nitEmpresa + " - " + nombre);

            } catch (Exception e) {
                System.err.println("Error al convertir fila de empresa: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return empresas;
    }
}