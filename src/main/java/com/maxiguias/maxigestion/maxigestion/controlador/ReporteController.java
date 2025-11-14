package com.maxiguias.maxigestion.maxigestion.controlador;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.maxiguias.maxigestion.maxigestion.modelo.EstadoOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.Orden;
import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.OrdenRepository;
import com.maxiguias.maxigestion.maxigestion.servicio.OrdenService;
import jakarta.servlet.http.HttpServletResponse;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.DetalleOrdenRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.ProductoRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.TerminadoRepository;
import com.maxiguias.maxigestion.maxigestion.dto.ProductoVendidoDTO;
import com.maxiguias.maxigestion.maxigestion.modelo.Terminado;

@Controller
@RequestMapping("/reportes")
public class ReporteController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private DetalleOrdenRepository detalleOrdenRepository;

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private TerminadoRepository terminadoRepository;

    @GetMapping
    public String listarReportes(Model model) {
        return "reportes";
    }

    @GetMapping("/")
    public String listarReportesConBarra(Model model) {
        return "reportes";
    }

    @GetMapping("/estadisticas-clientes")
    @ResponseBody
    public Map<String, Object> obtenerEstadisticas(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {

        Map<String, Object> estadisticas = new HashMap<>();

        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = LocalDate.parse(fechaInicio).atStartOfDay();
            LocalDateTime fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);

            Long naturales = usuarioRepository.countNaturalesByFechaRegistroBetween(inicio, fin);
            Long juridicos = usuarioRepository.countJuridicosByFechaRegistroBetween(inicio, fin);

            naturales = naturales != null ? naturales : 0;
            juridicos = juridicos != null ? juridicos : 0;

            estadisticas.put("naturales", naturales);
            estadisticas.put("juridicos", juridicos);
            estadisticas.put("total", naturales + juridicos);

        } else {
            Long naturales = usuarioRepository.countByTipoUsuario_Id(2);
            Long juridicos = usuarioRepository.countByTipoUsuario_Id(3);

            naturales = naturales != null ? naturales : 0;
            juridicos = juridicos != null ? juridicos : 0;

            estadisticas.put("naturales", naturales);
            estadisticas.put("juridicos", juridicos);
            estadisticas.put("total", naturales + juridicos);
        }

        return estadisticas;
    }

    @GetMapping("/exportar-excel")
    public void exportarExcelClientes(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            HttpServletResponse response) throws IOException {

        // Configurar respuesta
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "reporte_clientes_" + LocalDate.now() + ".xlsx";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        List<Usuario> usuariosNaturales;
        List<Usuario> usuariosJuridicos;

        // Si hay rango de fechas, filtramos por fechas
        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = LocalDate.parse(fechaInicio).atStartOfDay();
            LocalDateTime fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);

            usuariosNaturales = usuarioRepository.findNaturalesByFechaRegistroBetween(inicio, fin);
            usuariosJuridicos = usuarioRepository.findJuridicosByFechaRegistroBetween(inicio, fin);

        } else {
            // Si no hay rango → traer todos
            usuariosNaturales = usuarioRepository.findByTipoUsuario_Id(2);
            usuariosJuridicos = usuarioRepository.findByTipoUsuario_Id(3);
        }

        // Crear Excel
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Clientes");

            // Estilo encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Encabezados
            String[] headers = {
                    "Documento", "Nombre", "Primer Apellido", "Segundo Apellido",
                    "Dirección", "Teléfono", "Perfil", "Ciudad", "Fecha Registro"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;

            // Agregar Usuarios Naturales
            for (Usuario usuario : usuariosNaturales) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(usuario.getDocumento());
                row.createCell(1).setCellValue(usuario.getNombre());
                row.createCell(2).setCellValue(usuario.getPrimerApellido() != null ? usuario.getPrimerApellido() : "");
                row.createCell(3)
                        .setCellValue(usuario.getSegundoApellido() != null ? usuario.getSegundoApellido() : "");
                row.createCell(4).setCellValue(usuario.getDireccion() != null ? usuario.getDireccion() : "");
                row.createCell(5).setCellValue(usuario.getTelefono() != null ? usuario.getTelefono().toString() : "");
                row.createCell(6).setCellValue("Natural");
                row.createCell(7).setCellValue(usuario.getCiudad() != null ? usuario.getCiudad().getNombre() : "");
                row.createCell(8)
                        .setCellValue(usuario.getFechaRegistro() != null ? usuario.getFechaRegistro().toString() : "");
            }

            // Agregar Usuarios Jurídicos
            for (Usuario usuario : usuariosJuridicos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(usuario.getDocumento());
                row.createCell(1).setCellValue(usuario.getNombre());
                row.createCell(2).setCellValue(""); // No aplica
                row.createCell(3).setCellValue(""); // No aplica
                row.createCell(4).setCellValue(usuario.getDireccion() != null ? usuario.getDireccion() : "");
                row.createCell(5).setCellValue(usuario.getTelefono() != null ? usuario.getTelefono().toString() : "");
                row.createCell(6).setCellValue("Jurídico");
                row.createCell(7).setCellValue(usuario.getCiudad() != null ? usuario.getCiudad().getNombre() : "");
                row.createCell(8)
                        .setCellValue(usuario.getFechaRegistro() != null ? usuario.getFechaRegistro().toString() : "");
            }

            // Auto ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

    // ========== MÉTODOS PARA REPORTE DE ÓRDENES ==========

    @GetMapping("/estadisticas-ordenes")
    @ResponseBody
    public Map<String, Long> obtenerEstadisticasOrdenes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        Map<String, Long> estadisticas = new HashMap<>();

        if (fechaInicio != null && fechaFin != null) {
            Long totalOrdenes = ordenRepository.countByFechaOrdenBetween(fechaInicio.atStartOfDay(),
                    fechaFin.atTime(23, 59, 59));
            estadisticas.put("total", totalOrdenes);
        } else {
            estadisticas.put("total", ordenRepository.count());
        }

        return estadisticas;
    }

    @GetMapping("/exportar-zip-ordenes")
    public void exportarZipOrdenes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response) throws IOException {

        List<Orden> ordenes = (fechaInicio != null && fechaFin != null)
                ? ordenRepository.findByFechaOrdenBetween(fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59))
                : ordenRepository.findAll();

        // Validación si no se encuentran registros
        if (ordenes.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "No se encontraron órdenes para la fecha seleccionada.");
            return;
        }

        // Configurar la respuesta HTTP para exportar ZIP
        response.setContentType("application/zip");
        String fileName = "ordenes_" + fechaInicio + ".zip";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        // Generar ZIP con los PDFs
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (Orden orden : ordenes) {
                try {
                    byte[] pdfBytes = ordenService.generarOrdenPDF(orden.getId());
                    String entryName = "orden_" + orden.getId() + ".pdf";
                    zipOut.putNextEntry(new ZipEntry(entryName));
                    zipOut.write(pdfBytes);
                    zipOut.closeEntry();
                } catch (Exception e) {
                    System.err.println("Error generando PDF para orden " + orden.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    // ========== MÉTODOS PARA REPORTE DE VENTAS AL MES ===========

@GetMapping("/estadisticas-ventas")
@ResponseBody
public Map<String, Object> obtenerEstadisticasVentas(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

    Map<String, Object> response = new HashMap<>();

    EstadoOrden facturada = EstadoOrden.FACTURADA;
    EstadoOrden finalizada = EstadoOrden.FINALIZADA;

    Long total;

    if (fechaInicio != null && fechaFin != null) {
        // 🔸 Convertir LocalDate a LocalDateTime para cubrir todo el rango
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX); // hasta 23:59:59
        total = ordenRepository.contarPorEstadoYRangoFechas(facturada, finalizada, inicio, fin);
    } else {
        total = ordenRepository.contarPorEstado(facturada, finalizada);
    }

    response.put("total", total);
    return response;
}



    @GetMapping("/exportar-zip-ventas")
    public void exportarZipVentas(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            HttpServletResponse response) throws IOException {

        // Configurar respuesta ZIP
        response.setContentType("application/zip");
        String fileName = "ventas_mes_" + LocalDate.now() + ".zip";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        List<Orden> ordenes;

        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = LocalDate.parse(fechaInicio).atStartOfDay();
            LocalDateTime fin = LocalDate.parse(fechaFin).atTime(23, 59, 59);
            ordenes = ordenRepository.findVentasDelMes(inicio, fin);
        } else {
            // Si no se envían fechas, traer todo lo facturado y finalizado
            ordenes = ordenRepository.findAll().stream()
                    .filter(o -> {
                        EstadoOrden estado = o.getEstado();
                        return estado == EstadoOrden.FACTURADA || estado == EstadoOrden.FINALIZADA;
                    })
                    .toList();

        }

        // Crear ZIP
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (Orden orden : ordenes) {
                try {
                    byte[] pdfBytes = ordenService.generarOrdenPDF(orden.getId());
                    String entryName = "orden_" + orden.getId() + "_" + orden.getFechaOrden() + ".pdf";
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(pdfBytes);
                    zipOut.closeEntry();
                } catch (Exception e) {
                    System.err.println("Error generando PDF para orden " + orden.getId() + ": " + e.getMessage());
                }
            }
        }
    }
    // ========== MÉTODOS PARA REPORTE DE PRODUCTOS MÁS VENDIDOS ==========

    @GetMapping("/estadisticas-productos")
    @ResponseBody
    public Map<String, Object> obtenerEstadisticasProductos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        Map<String, Object> estadisticas = new HashMap<>();
        Long totalProductosVendidos;
        List<Object[]> productosVendidos;

        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);

            totalProductosVendidos = detalleOrdenRepository.countTotalProductosVendidosEntreFechas(inicio, fin);
            productosVendidos = detalleOrdenRepository.findProductosMasVendidosEntreFechas(inicio, fin);

        } else {
            totalProductosVendidos = detalleOrdenRepository.countTotalProductosVendidos();
            productosVendidos = detalleOrdenRepository.findProductosMasVendidos();
        }

        estadisticas.put("total", totalProductosVendidos != null ? totalProductosVendidos : 0);
        estadisticas.put("productos", convertirAProductosVendidosDTO(productosVendidos));

        return estadisticas;
    }

    @GetMapping("/exportar-excel-productos")
    public void exportarExcelProductos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "productos_mas_vendidos_" + LocalDate.now() + ".xlsx";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        List<Object[]> productosVendidos;

        if (fechaInicio != null && fechaFin != null) {
            productosVendidos = detalleOrdenRepository.findProductosMasVendidosEntreFechas(
                    fechaInicio.atStartOfDay(),
                    fechaFin.atTime(23, 59, 59));
        } else {
            productosVendidos = detalleOrdenRepository.findProductosMasVendidos();
        }

        // Crear el libro de Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos Más Vendidos");

            // Crear estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Crear fila de encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = { "Posición", "Producto", "Medida", "Cantidad Vendida" };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agregar datos de productos vendidos
            int rowNum = 1;
            int posicion = 1;
            for (Object[] productoData : productosVendidos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(posicion++);
                row.createCell(1).setCellValue((String) productoData[0]); // nombre_guia
                row.createCell(2).setCellValue(((BigDecimal) productoData[1]).doubleValue()); // medida
                row.createCell(3).setCellValue(((Long) productoData[2]).intValue()); // cantidad vendida
            }

            // Ajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir el archivo
            workbook.write(response.getOutputStream());
        }
    }

    // Método auxiliar para convertir resultados de consulta a DTOs
    private List<ProductoVendidoDTO> convertirAProductosVendidosDTO(List<Object[]> resultados) {
        List<ProductoVendidoDTO> productos = new ArrayList<>();
        for (Object[] resultado : resultados) {
            String nombreProducto = (String) resultado[0];
            BigDecimal medida = (BigDecimal) resultado[1];
            Long cantidadVendida = (Long) resultado[2];
            productos.add(new ProductoVendidoDTO(nombreProducto, medida, cantidadVendida));
        }
        return productos;
    }

    // ========== MÉTODOS PARA REPORTE DE CATÁLOGO DE PRODUCTOS ==========

    @GetMapping("/exportar-catalogo-naturales")
    public void exportarCatalogoNaturales(HttpServletResponse response) throws IOException {
        // Configurar la respuesta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "catalogo_usuarios_naturales_" + LocalDate.now() + ".xlsx";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        // Obtener todos los terminados con sus productos
        List<Terminado> terminados = terminadoRepository.findAll();

        // Crear el libro de Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catálogo Usuarios Naturales");

            // Crear estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Crear fila de encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = { "Nombre del Producto", "Medida del Producto", "Precio Público" };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agregar datos de productos
            int rowNum = 1;
            for (Terminado terminado : terminados) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(terminado.getProducto().getNombre());
                row.createCell(1).setCellValue(terminado.getMedidaTerminadoProducto().doubleValue());
                row.createCell(2).setCellValue(terminado.getPrecioPublico());
            }

            // Ajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir el archivo
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/exportar-catalogo-juridicos")
    public void exportarCatalogoJuridicos(HttpServletResponse response) throws IOException {
        // Configurar la respuesta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "catalogo_usuarios_juridicos_" + LocalDate.now() + ".xlsx";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        // Obtener todos los terminados con sus productos
        List<Terminado> terminados = terminadoRepository.findAll();

        // Crear el libro de Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Catálogo Usuarios Jurídicos");

            // Crear estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle percentageStyle = workbook.createCellStyle();
            percentageStyle.setDataFormat(workbook.createDataFormat().getFormat("0%"));

            // Crear fila de encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = { "Nombre del Producto", "Medida del Producto", "Precio Público", "Precio por Mayor",
                    "% Ganancia al por Mayor", "Precio por Encargo", "% Ganancia por Encargo" };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agregar datos de productos
            int rowNum = 1;
            for (Terminado terminado : terminados) {
                Row row = sheet.createRow(rowNum++);

                double precioPublico = terminado.getPrecioPublico();
                double precioMayor = terminado.getPrecioPorMayor();
                double precioEncargo = terminado.getPrecioPorEncargo();

                double gananciaMayorPct = (precioPublico > 0) ? (precioPublico - precioMayor) / precioPublico : 0;
                double gananciaEncargoPct = (precioPublico > 0) ? precioEncargo / precioPublico : 0;

                row.createCell(0).setCellValue(terminado.getProducto().getNombre());
                row.createCell(1).setCellValue(terminado.getMedidaTerminadoProducto().doubleValue());
                row.createCell(2).setCellValue(terminado.getPrecioPublico());
                row.createCell(3).setCellValue(terminado.getPrecioPorMayor());
                Cell cellGananciaMayor = row.createCell(4);
                cellGananciaMayor.setCellValue(gananciaMayorPct);
                cellGananciaMayor.setCellStyle(percentageStyle);
                row.createCell(5).setCellValue(terminado.getPrecioPorEncargo());
                Cell cellGananciaEncargo = row.createCell(6);
                cellGananciaEncargo.setCellValue(gananciaEncargoPct);
                cellGananciaEncargo.setCellStyle(percentageStyle);
            }

            // Ajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir el archivo
            workbook.write(response.getOutputStream());
        }
    }
}