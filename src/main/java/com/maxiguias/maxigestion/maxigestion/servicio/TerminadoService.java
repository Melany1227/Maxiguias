package com.maxiguias.maxigestion.maxigestion.servicio;
import java.util.List;

import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.Terminado;
import com.maxiguias.maxigestion.maxigestion.repositorio.TerminadoRepository;


@Service
public class TerminadoService {
    private final TerminadoRepository terminadoRepository;

    public TerminadoService(TerminadoRepository terminadoRepository) {
        this.terminadoRepository = terminadoRepository;
    }

    public List<Terminado> obtenerTodosLosTerminados() {
        return terminadoRepository.findAll();
    }

    public List<Terminado> obtenerTerminadosPorProducto(Long productoId) {
        return terminadoRepository.findByProductoId(productoId);
    }

}
