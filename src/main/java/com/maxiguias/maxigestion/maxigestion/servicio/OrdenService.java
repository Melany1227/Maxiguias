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
import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.DetalleOrdenRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.OrdenRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;

@Service
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private DetalleOrdenRepository detalleOrdenRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;

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
        
        // Validar que existe un usuario representante configurado
        Long countRepresentante = usuarioRepository.countByRepresentanteProfile();
        if (countRepresentante == null || countRepresentante == 0) {
            throw new RuntimeException("No se puede generar la factura: Es necesario configurar un usuario con perfil de REPRESENTANTE en el sistema.");
        }
        
        // Obtener el usuario representante
        Usuario representante = usuarioRepository.findRepresentante()
            .orElseThrow(() -> new RuntimeException("No se puede encontrar el usuario representante configurado"));

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

            // Información del representante
            Paragraph representativeTitle = new Paragraph("INFORMACIÓN DEL REPRESENTANTE", headerFont);
            representativeTitle.setSpacingBefore(10);
            representativeTitle.setSpacingAfter(5);
            document.add(representativeTitle);

            PdfPTable representativeTable = new PdfPTable(2);
            representativeTable.setWidthPercentage(100);
            representativeTable.setSpacingAfter(15);

            // Nombre completo del representante
            String nombreCompletoRepresentante = buildFullName(
                representante.getNombre(), 
                representante.getPrimerApellido(), 
                representante.getSegundoApellido()
            );

            representativeTable.addCell(createInfoCell("Documento: " + representante.getDocumento(), normalFont));
            representativeTable.addCell(createInfoCell("Nombre: " + nombreCompletoRepresentante, normalFont));
            String telefonoRepresentante = safeString(representante.getTelefono().toString());
            representativeTable.addCell(createInfoCell("Teléfono: " + (telefonoRepresentante.isEmpty() ? "N/A" : telefonoRepresentante), normalFont));
            
            String correoRepresentante = safeString(representante.getCorreo());
            representativeTable.addCell(createInfoCell("Correo: " + (correoRepresentante.isEmpty() ? "N/A" : correoRepresentante), normalFont));
            
            String ciudadRepresentante = representante.getCiudad() != null ? safeString(representante.getCiudad().getNombre()) : "";
            representativeTable.addCell(createInfoCell("Ciudad: " + (ciudadRepresentante.isEmpty() ? "N/A" : ciudadRepresentante), normalFont));
            
            String direccionRepresentante = safeString(representante.getDireccion());
            representativeTable.addCell(createInfoCell("Dirección: " + (direccionRepresentante.isEmpty() ? "N/A" : direccionRepresentante), normalFont));

            document.add(representativeTable);

            // Información de la orden
            Paragraph orderTitle = new Paragraph("INFORMACIÓN DE LA ORDEN", headerFont);
            orderTitle.setSpacingBefore(10);
            orderTitle.setSpacingAfter(5);
            document.add(orderTitle);

            PdfPTable orderTable = new PdfPTable(2);
            orderTable.setWidthPercentage(100);
            orderTable.setSpacingAfter(15);

            orderTable.addCell(createInfoCell("Número de Orden: #" + orden.getId(), normalFont));
            
            String fechaEntrega = orden.getFechaEntrega() != null ? orden.getFechaEntrega().toString() : "N/A";
            orderTable.addCell(createInfoCell("Fecha de Entrega: " + fechaEntrega, normalFont));
            
            String descripcionVenta = safeString(orden.getDescripcionVenta());
            orderTable.addCell(createInfoCell("Descripción de Venta: " + (descripcionVenta.isEmpty() ? "N/A" : descripcionVenta), normalFont));

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
            String nombreCompletoCliente = buildFullName(
                orden.getUsuario().getNombre(), 
                orden.getUsuario().getPrimerApellido(), 
                orden.getUsuario().getSegundoApellido()
            );

            customerTable.addCell(createInfoCell("Nombre: " + nombreCompletoCliente, normalFont));
            customerTable.addCell(createInfoCell("Documento: " + orden.getUsuario().getDocumento(), normalFont));
            
            String telefonoCliente = safeString(orden.getUsuario().getTelefono().toString());
            customerTable.addCell(createInfoCell("Teléfono: " + (telefonoCliente.isEmpty() ? "N/A" : telefonoCliente), normalFont));
            
            String correoCliente = safeString(orden.getUsuario().getCorreo());
            customerTable.addCell(createInfoCell("Correo Electrónico: " + (correoCliente.isEmpty() ? "N/A" : correoCliente), normalFont));
            
            String direccionCliente = safeString(orden.getUsuario().getDireccion());
            customerTable.addCell(createInfoCell("Dirección: " + (direccionCliente.isEmpty() ? "N/A" : direccionCliente), normalFont));
            
            String ciudadCompleta = "";
            if (orden.getUsuario().getCiudad() != null) {
                String ciudad = safeString(orden.getUsuario().getCiudad().getNombre());
                String departamento = orden.getUsuario().getCiudad().getDepartamento() != null ? 
                    safeString(orden.getUsuario().getCiudad().getDepartamento().getNombre()) : "";
                ciudadCompleta = ciudad + (departamento.isEmpty() ? "" : " - " + departamento);
            }
            customerTable.addCell(createInfoCell("Ciudad: " + (ciudadCompleta.isEmpty() ? "N/A" : ciudadCompleta), normalFont));
            
            String fechaRegistro = orden.getUsuario().getFechaRegistro() != null ? orden.getUsuario().getFechaRegistro().toString() : "N/A";
            customerTable.addCell(createInfoCell("Fecha de Registro: " + fechaRegistro, normalFont));

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
                String nombreProducto = "";
                if (detalle.getTerminado() != null && detalle.getTerminado().getProducto() != null) {
                    nombreProducto = safeString(detalle.getTerminado().getProducto().getNombre());
                }
                if (nombreProducto.isEmpty()) {
                    nombreProducto = "Producto personalizado";
                }

                // Medida del producto (si existe)
                String medida = "";
                if (detalle.getTerminado() != null && detalle.getTerminado().getMedidaTerminadoProducto() != null) {
                    medida = detalle.getTerminado().getMedidaTerminadoProducto().toString();
                }
                if (medida.isEmpty()) {
                    medida = "N/A";
                }

                productosTable.addCell(createDataCell(nombreProducto, normalFont));
                
                String descripcionDetalle = safeString(detalle.getDescripcion());
                productosTable.addCell(createDataCell(descripcionDetalle.isEmpty() ? "N/A" : descripcionDetalle, normalFont));
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
            String firmaDigital = safeString(orden.getFirmaDigital());
            if (!firmaDigital.isEmpty()) {
                Paragraph firmaTitle = new Paragraph("FIRMA DIGITAL", headerFont);
                firmaTitle.setSpacingBefore(20);
                firmaTitle.setSpacingAfter(5);
                document.add(firmaTitle);

                Paragraph firma = new Paragraph(firmaDigital, smallFont);
                firma.setSpacingAfter(10);
                document.add(firma);
            }

            // Líneas de firma
            PdfPTable firmasTable = new PdfPTable(2);
            firmasTable.setWidthPercentage(100);
            firmasTable.setSpacingBefore(30);
            firmasTable.setSpacingAfter(20);
            
            // Celda para firma del representante
            PdfPCell firmaRepresentanteCell = new PdfPCell();
            firmaRepresentanteCell.setBorder(Rectangle.NO_BORDER);
            firmaRepresentanteCell.setPaddingBottom(10);
            
            Paragraph firmaRepresentanteTitle = new Paragraph("Firma del Representante:", normalFont);
            firmaRepresentanteTitle.setSpacingAfter(30);
            firmaRepresentanteCell.addElement(firmaRepresentanteTitle);
            
            // Línea para la firma del representante
            Paragraph lineaRepresentante = new Paragraph("_________________________________", normalFont);
            lineaRepresentante.setAlignment(Element.ALIGN_CENTER);
            firmaRepresentanteCell.addElement(lineaRepresentante);
            
            // Usar el nombre completo ya construido sin nulls
            Paragraph nombreRepresentante = new Paragraph(nombreCompletoRepresentante.isEmpty() ? "N/A" : nombreCompletoRepresentante, smallFont);
            nombreRepresentante.setAlignment(Element.ALIGN_CENTER);
            nombreRepresentante.setSpacingBefore(5);
            firmaRepresentanteCell.addElement(nombreRepresentante);
            
            firmasTable.addCell(firmaRepresentanteCell);
            
            // Celda para firma del cliente
            PdfPCell firmaClienteCell = new PdfPCell();
            firmaClienteCell.setBorder(Rectangle.NO_BORDER);
            firmaClienteCell.setPaddingBottom(10);
            
            Paragraph firmaClienteTitle = new Paragraph("Firma del Cliente:", normalFont);
            firmaClienteTitle.setSpacingAfter(30);
            firmaClienteCell.addElement(firmaClienteTitle);
            
            // Línea para la firma del cliente
            Paragraph lineaCliente = new Paragraph("_________________________________", normalFont);
            lineaCliente.setAlignment(Element.ALIGN_CENTER);
            firmaClienteCell.addElement(lineaCliente);
            
            // Usar el nombre completo del cliente ya construido sin nulls (reutilizamos la variable ya creada)
            Paragraph nombreCliente = new Paragraph(nombreCompletoCliente.isEmpty() ? "N/A" : nombreCompletoCliente, smallFont);
            nombreCliente.setAlignment(Element.ALIGN_CENTER);
            nombreCliente.setSpacingBefore(5);
            firmaClienteCell.addElement(nombreCliente);
            
            firmasTable.addCell(firmaClienteCell);
            
            document.add(firmasTable);

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

    // Método helper para evitar mostrar null en el PDF
    private String safeString(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : "";
    }

    // Método helper para crear nombres completos sin null
    private String buildFullName(String nombre, String primerApellido, String segundoApellido) {
        StringBuilder fullName = new StringBuilder();
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            fullName.append(nombre.trim());
        }
        
        if (primerApellido != null && !primerApellido.trim().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(primerApellido.trim());
        }
        
        if (segundoApellido != null && !segundoApellido.trim().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(segundoApellido.trim());
        }
        
        return fullName.toString();
    }

    private void addWatermark(PdfWriter writer, Document document) {
        try {
            // Obtener el logo desde el classpath como InputStream
            java.io.InputStream logoInputStream = getClass().getClassLoader().getResourceAsStream("static/img/logo.png");
            
            if (logoInputStream == null) {
                System.err.println("Logo no encontrado en el classpath: static/img/logo.png");
                return;
            }
            
            // Crear la imagen desde el InputStream
            Image watermarkImage = Image.getInstance(logoInputStream.readAllBytes());
            logoInputStream.close();
            
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