package com.maxiguias.maxigestion.maxigestion.servicio;

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleFactura;
import com.maxiguias.maxigestion.maxigestion.modelo.Factura;
import com.maxiguias.maxigestion.maxigestion.repositorio.DetalleFacturaRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.FacturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private DetalleFacturaRepository detalleFacturaRepository;

    public Factura crearFactura(Factura factura, List<DetalleFactura> detalles) {
        Factura facturaGuardada = facturaRepository.save(factura);

        for (DetalleFactura detalle : detalles) {
            detalle.setFactura(facturaGuardada);
            detalleFacturaRepository.save(detalle);
        }

        return facturaGuardada;
    }
}

