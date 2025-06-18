package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleFactura;
import com.maxiguias.maxigestion.maxigestion.modelo.Factura;
import com.maxiguias.maxigestion.maxigestion.repositorio.DetalleFacturaRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.FacturaRepository;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private DetalleFacturaRepository detalleFacturaRepository;

    public Factura guardarFactura(Factura factura) {
        return facturaRepository.save(factura); 
    }

    public void guardarDetalles(DetalleFactura detalles) {
        detalleFacturaRepository.save(detalles); 
    }

    public List<Factura> obtenerTodasLasFacturas() {
        return facturaRepository.findAll();
    }
    
    public Factura obtenerFacturaPorId(Long id) {
        return facturaRepository.findById(id).orElse(null);
    }


}

