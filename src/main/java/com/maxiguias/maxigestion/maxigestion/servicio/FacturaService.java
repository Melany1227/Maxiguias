package com.maxiguias.maxigestion.maxigestion.servicio;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;

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

    public byte[] generarFacturaPDF(Long facturaId) {
        Factura factura = obtenerFacturaPorId(facturaId);
        if (factura == null) {
            throw new RuntimeException("Factura no encontrada");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);

            Paragraph title = new Paragraph("DETALLE DE FACTURA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(10);

            infoTable.addCell(createInfoCell("Cliente: " + 
                    factura.getUsuario().getNombre() + " " + factura.getUsuario().getPrimerApellido(), normalFont));
            infoTable.addCell(createInfoCell("Ciudad: " + factura.getCiudad().getNombre(), normalFont));
            infoTable.addCell(createInfoCell("Empresa: " + factura.getEmpresa().getNombreEmpresa(), normalFont));
            infoTable.addCell(createInfoCell("Total: $" + factura.getTotalFactura(), normalFont));
            infoTable.addCell(createInfoCell("Fecha: " + factura.getFechaVenta().toString(), normalFont));
            infoTable.addCell(createInfoCell("Descripción Venta: " + factura.getDescripcionVenta(), normalFont));

            document.add(infoTable);

            Paragraph productosTitle = new Paragraph("PRODUCTOS FACTURADOS", headerFont);
            productosTitle.setSpacingBefore(20);
            productosTitle.setSpacingAfter(10);
            document.add(productosTitle);

            PdfPTable productosTable = new PdfPTable(4);
            productosTable.setWidthPercentage(100);
            productosTable.setSpacingBefore(10);

            productosTable.addCell(createHeaderCell("Descripción", headerFont));
            productosTable.addCell(createHeaderCell("Cantidad", headerFont));
            productosTable.addCell(createHeaderCell("Valor", headerFont));
            productosTable.addCell(createHeaderCell("Total", headerFont));

            for (DetalleFactura detalle : factura.getDetalles()) {
                double total = detalle.getCantidad() * detalle.getValor().doubleValue();
                productosTable.addCell(createDataCell(detalle.getDescripcion(), normalFont));
                productosTable.addCell(createDataCell(String.valueOf(detalle.getCantidad()), normalFont));
                productosTable.addCell(createDataCell(detalle.getValor().toString(), normalFont));
                productosTable.addCell(createDataCell(String.valueOf(total), normalFont));
            }

            document.add(productosTable);
            document.close();

        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF", e);
        }

        return baos.toByteArray();
    }

    private PdfPCell createInfoCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(8);
        cell.setBorder(0);
        return cell;
    }

    private PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        return cell;
    }

    private PdfPCell createDataCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        return cell;
    }

}

