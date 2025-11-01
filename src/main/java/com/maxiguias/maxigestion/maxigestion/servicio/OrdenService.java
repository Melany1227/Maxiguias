package com.maxiguias.maxigestion.maxigestion.servicio;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.EstadoOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.Orden;
import com.maxiguias.maxigestion.maxigestion.repositorio.DetalleOrdenRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.OrdenRepository;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private DetalleOrdenRepository detalleOrdenRepository;

    @Transactional
    public Orden guardarOrden(Orden orden) {
        return ordenRepository.save(orden); 
    }

    @Transactional
    public void guardarDetalles(DetalleOrden detalles) {
        detalleOrdenRepository.save(detalles); 
    }

    public List<Orden> obtenerTodasLasOrdenes() {
        return ordenRepository.findAll();
    }

    public Page<Orden> obtenerOrdenesPaginadas(Pageable pageable) {
        return ordenRepository.findAll(pageable);
    }

    public Page<Orden> buscarOrdenesPorCliente(String termino, Pageable pageable) {
        return ordenRepository.findByClienteTermino(termino, pageable);
    }

    public Page<Orden> filtrarOrdenesPorClienteDocumento(Long clienteDocumento, Pageable pageable) {
        return ordenRepository.findByUsuarioDocumento(clienteDocumento, pageable);
    }

    public Page<Orden> obtenerOrdenesPorUsuario(Long usuarioDocumento, Pageable pageable) {
        return ordenRepository.findByUsuario_Documento(usuarioDocumento, pageable);
    }

    public Page<Orden> buscarOrdenesPorClienteYUsuario(String termino, Long usuarioDocumento, Pageable pageable) {
        return ordenRepository.findByClienteTerminoAndUsuario(termino, usuarioDocumento, pageable);
    }
    
    public Orden obtenerOrdenPorId(Long id) {
        return ordenRepository.findById(id).orElse(null);
    }

    @Transactional
    public void actualizarEstadoOrden(Long id, EstadoOrden nuevoEstado) {
        Orden orden = obtenerOrdenPorId(id);
        if (orden != null) {
            orden.setEstado(nuevoEstado);
            ordenRepository.save(orden);
        }
    }

    @Transactional
    public void eliminarDetallesOrden(Long ordenId) {
        detalleOrdenRepository.deleteByOrdenId(ordenId);
    }

    public byte[] generarOrdenPDF(Long ordenId) {
        Orden orden = obtenerOrdenPorId(ordenId);
        if (orden == null) {
            throw new RuntimeException("Orden no encontrada");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            
            // Agregar marca de agua con el logo
            addWatermark(writer, document);

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 10);
            Font smallFont = new Font(Font.FontFamily.HELVETICA, 8);

            // Título principal
            Paragraph title = new Paragraph("FACTURA DE ORDEN", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Información de la empresa
            Paragraph companyTitle = new Paragraph("INFORMACIÓN DE LA EMPRESA", headerFont);
            companyTitle.setSpacingBefore(10);
            companyTitle.setSpacingAfter(5);
            document.add(companyTitle);

            PdfPTable companyTable = new PdfPTable(2);
            companyTable.setWidthPercentage(100);
            companyTable.setSpacingAfter(15);

            companyTable.addCell(createInfoCell("Empresa: " + orden.getEmpresa().getNombreEmpresa(), normalFont));
            companyTable.addCell(createInfoCell("NIT: " + orden.getEmpresa().getNitEmpresa(), normalFont));

            document.add(companyTable);

            // Información de la orden
            Paragraph orderTitle = new Paragraph("INFORMACIÓN DE LA ORDEN", headerFont);
            orderTitle.setSpacingBefore(10);
            orderTitle.setSpacingAfter(5);
            document.add(orderTitle);

            PdfPTable orderTable = new PdfPTable(2);
            orderTable.setWidthPercentage(100);
            orderTable.setSpacingAfter(15);

            orderTable.addCell(createInfoCell("Número de Orden: #" + orden.getId(), normalFont));
            orderTable.addCell(createInfoCell("Estado: " + orden.getEstado().getDescripcion(), normalFont));
            orderTable.addCell(createInfoCell("Fecha de Orden: " + 
                    (orden.getFechaOrden() != null ? orden.getFechaOrden().toString() : "N/A"), normalFont));
            orderTable.addCell(createInfoCell("Fecha de Entrega: " + 
                    (orden.getFechaEntrega() != null ? orden.getFechaEntrega().toString() : "N/A"), normalFont));
            orderTable.addCell(createInfoCell("Descripción de Venta: " + 
                    (orden.getDescripcionVenta() != null ? orden.getDescripcionVenta() : "N/A"), normalFont));

            document.add(orderTable);

            // Información completa del cliente
            Paragraph customerTitle = new Paragraph("INFORMACIÓN DEL CLIENTE", headerFont);
            customerTitle.setSpacingBefore(10);
            customerTitle.setSpacingAfter(5);
            document.add(customerTitle);

            PdfPTable customerTable = new PdfPTable(2);
            customerTable.setWidthPercentage(100);
            customerTable.setSpacingAfter(15);

            // Nombre completo del cliente
            String nombreCompleto = orden.getUsuario().getNombre() + " " + 
                    orden.getUsuario().getPrimerApellido() + 
                    (orden.getUsuario().getSegundoApellido() != null ? " " + orden.getUsuario().getSegundoApellido() : "");

            customerTable.addCell(createInfoCell("Nombre: " + nombreCompleto, normalFont));
            customerTable.addCell(createInfoCell("Documento: " + orden.getUsuario().getDocumento(), normalFont));
            customerTable.addCell(createInfoCell("Teléfono: " + 
                    (orden.getUsuario().getTelefono() != null ? orden.getUsuario().getTelefono() : "N/A"), normalFont));
            customerTable.addCell(createInfoCell("Correo Electrónico: " + 
                    (orden.getUsuario().getCorreo() != null ? orden.getUsuario().getCorreo() : "N/A"), normalFont));
            customerTable.addCell(createInfoCell("Dirección: " + 
                    (orden.getUsuario().getDireccion() != null ? orden.getUsuario().getDireccion() : "N/A"), normalFont));
            customerTable.addCell(createInfoCell("Ciudad: " + orden.getUsuario().getCiudad().getNombre() + " - " + orden.getUsuario().getCiudad().getDepartamento().getNombre(), normalFont));
            customerTable.addCell(createInfoCell("Fecha de Registro: " + 
                    (orden.getUsuario().getFechaRegistro() != null ? orden.getUsuario().getFechaRegistro().toString() : "N/A"), normalFont));

            document.add(customerTable);

            // Detalle de productos y servicios
            Paragraph productosTitle = new Paragraph("DETALLE DE PRODUCTOS", headerFont);
            productosTitle.setSpacingBefore(20);
            productosTitle.setSpacingAfter(10);
            document.add(productosTitle);

            // Tabla más detallada de productos
            PdfPTable productosTable = new PdfPTable(6);
            productosTable.setWidthPercentage(100);
            productosTable.setSpacingBefore(10);

            // Configurar anchos de columnas para mejor visualización
            float[] columnWidths = {3f, 2f, 1.5f, 1.5f, 1.5f, 1.5f};
            productosTable.setWidths(columnWidths);

            // Headers de la tabla
            productosTable.addCell(createHeaderCell("Producto/Servicio", headerFont));
            productosTable.addCell(createHeaderCell("Descripción", headerFont));
            productosTable.addCell(createHeaderCell("Medida", headerFont));
            productosTable.addCell(createHeaderCell("Cantidad", headerFont));
            productosTable.addCell(createHeaderCell("Valor Unitario", headerFont));
            productosTable.addCell(createHeaderCell("Total", headerFont));

            // Agregar filas de productos
            BigDecimal subtotal = BigDecimal.ZERO;
            for (DetalleOrden detalle : orden.getDetalles()) {
                BigDecimal total = detalle.getValor().multiply(BigDecimal.valueOf(detalle.getCantidad()));
                subtotal = subtotal.add(total);

                // Nombre del producto (si existe terminado)
                String nombreProducto = detalle.getTerminado() != null && detalle.getTerminado().getProducto() != null 
                    ? detalle.getTerminado().getProducto().getNombre() 
                    : "Producto personalizado";

                // Medida del producto (si existe)
                String medida = detalle.getTerminado() != null && detalle.getTerminado().getMedidaTerminadoProducto() != null
                    ? detalle.getTerminado().getMedidaTerminadoProducto().toString()
                    : "N/A";

                productosTable.addCell(createDataCell(nombreProducto, normalFont));
                productosTable.addCell(createDataCell(detalle.getDescripcion() != null ? detalle.getDescripcion() : "N/A", normalFont));
                productosTable.addCell(createDataCell(medida, normalFont));
                productosTable.addCell(createDataCell(String.valueOf(detalle.getCantidad()), normalFont));
                productosTable.addCell(createDataCell("$" + detalle.getValor().toString(), normalFont));
                productosTable.addCell(createDataCell("$" + total.toString(), normalFont));
            }

            document.add(productosTable);

            // Resumen financiero
            Paragraph resumenTitle = new Paragraph("RESUMEN FINANCIERO", headerFont);
            resumenTitle.setSpacingBefore(20);
            resumenTitle.setSpacingAfter(10);
            document.add(resumenTitle);

            PdfPTable resumenTable = new PdfPTable(2);
            resumenTable.setWidthPercentage(60);
            resumenTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resumenTable.setSpacingBefore(10);
            resumenTable.addCell(createInfoCell("TOTAL FACTURA:", titleFont));
            resumenTable.addCell(createInfoCell("$" + orden.getTotalFactura().toString(), titleFont));

            document.add(resumenTable);

            // Pie de página con información adicional
            if (orden.getFirmaDigital() != null && !orden.getFirmaDigital().isEmpty()) {
                Paragraph firmaTitle = new Paragraph("FIRMA DIGITAL", headerFont);
                firmaTitle.setSpacingBefore(20);
                firmaTitle.setSpacingAfter(5);
                document.add(firmaTitle);

                Paragraph firma = new Paragraph(orden.getFirmaDigital(), smallFont);
                firma.setSpacingAfter(10);
                document.add(firma);
            }

            // Nota final
            Paragraph nota = new Paragraph("Este documento es una factura generada automáticamente por el sistema de gestión de órdenes.", smallFont);
            nota.setAlignment(Element.ALIGN_CENTER);
            nota.setSpacingBefore(20);
            document.add(nota);
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

    private void addWatermark(PdfWriter writer, Document document) {
        try {
            // Obtener el path del logo desde el classpath
            String logoPath = getClass().getClassLoader().getResource("static/img/logo.png").getPath();
            
            // Crear la imagen desde el archivo
            Image watermarkImage = Image.getInstance(logoPath);
            
            // Configurar el tamaño de la marca de agua (más grande y ovalada)
            float imageWidth = 350f;  // Más ancho para forma ovalada
            float imageHeight = 250f; // Menos alto para forma ovalada
            watermarkImage.scaleToFit(imageWidth, imageHeight);
            
            // Calcular posición central
            Rectangle pageSize = document.getPageSize();
            float x = (pageSize.getWidth() - imageWidth) / 2;
            float y = (pageSize.getHeight() - imageHeight) / 2;
            
            // Configurar transparencia
            PdfContentByte canvas = writer.getDirectContentUnder();
            PdfGState gState = new PdfGState();
            gState.setFillOpacity(0.08f); // Un poco más transparente debido al tamaño mayor
            canvas.setGState(gState);
            
            // Posicionar la imagen como marca de agua
            watermarkImage.setAbsolutePosition(x, y);
            canvas.addImage(watermarkImage);
            
        } catch (Exception e) {
            // Si hay error con el logo, continúa sin marca de agua
            System.err.println("No se pudo cargar el logo para la marca de agua: " + e.getMessage());
        }
    }

}