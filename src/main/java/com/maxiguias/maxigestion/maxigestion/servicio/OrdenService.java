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

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.Orden;
import com.maxiguias.maxigestion.maxigestion.repositorio.DetalleOrdenRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.OrdenRepository;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private DetalleOrdenRepository detalleOrdenRepository;

    public Orden guardarOrden(Orden orden) {
        return ordenRepository.save(orden); 
    }

    public void guardarDetalles(DetalleOrden detalles) {
        detalleOrdenRepository.save(detalles); 
    }

    public List<Orden> obtenerTodasLasOrdenes() {
        return ordenRepository.findAll();
    }
    
    public Orden obtenerOrdenPorId(Long id) {
        return ordenRepository.findById(id).orElse(null);
    }

    public byte[] generarOrdenPDF(Long ordenId) {
        Orden orden = obtenerOrdenPorId(ordenId);
        if (orden == null) {
            throw new RuntimeException("Orden no encontrada");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);

            Paragraph title = new Paragraph("DETALLE DE ORDEN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(10);

            infoTable.addCell(createInfoCell("Cliente: " + 
                    orden.getUsuario().getNombre() + " " + orden.getUsuario().getPrimerApellido(), normalFont));
            infoTable.addCell(createInfoCell("Documento: " + orden.getUsuario().getDocumento(), normalFont));
            infoTable.addCell(createInfoCell("Dirección: " + orden.getUsuario().getDireccion(), normalFont));
            infoTable.addCell(createInfoCell("Ciudad: " + orden.getUsuario().getCiudad().getNombre(), normalFont));
            infoTable.addCell(createInfoCell("Departamento: " + orden.getUsuario().getCiudad().getDepartamento().getNombre(), normalFont));
            infoTable.addCell(createInfoCell("Empresa: " + orden.getEmpresa().getNombreEmpresa(), normalFont));
            infoTable.addCell(createInfoCell("Fecha: " + orden.getFechaEntrega().toString(), normalFont));
            infoTable.addCell(createInfoCell("Total: $" + orden.getTotalFactura(), normalFont));
            infoTable.addCell(createInfoCell("Descripción Venta: " + orden.getDescripcionVenta(), normalFont));

            document.add(infoTable);

            Paragraph productosTitle = new Paragraph("PRODUCTOS EN LA ORDEN", headerFont);
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

            for (DetalleOrden detalle : orden.getDetalles()) {
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