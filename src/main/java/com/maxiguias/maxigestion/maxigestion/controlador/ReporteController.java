package com.maxiguias.maxigestion.maxigestion.controlador;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.maxiguias.maxigestion.maxigestion.modelo.Orden;
import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.OrdenRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;
import com.maxiguias.maxigestion.maxigestion.servicio.OrdenService;
import jakarta.servlet.http.HttpServletResponse;
@Controller
@RequestMapping("/reportes")
public class ReporteController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private OrdenService ordenService;

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
    public Map<String, Long> obtenerEstadisticasClientes(@RequestParam(required = false) Integer mes) {
        Map<String, Long> estadisticas = new HashMap<>();
        
        if (mes != null && mes >= 1 && mes <= 12) {
            // Si se especifica un mes, filtrar por mes del año actual
            Integer anioActual = LocalDate.now().getYear();
            
            // Obtener usuarios naturales del mes especificado
            Long usuariosNaturales = usuarioRepository.countByTipoUsuarioAndMes(2, mes, anioActual);
            
            // Obtener usuarios jurídicos del mes especificado
            Long usuariosJuridicos = usuarioRepository.countByTipoUsuarioAndMes(3, mes, anioActual);
            
            // Total de usuarios del mes
            Long totalUsuarios = usuariosNaturales + usuariosJuridicos;
            
            estadisticas.put("total", totalUsuarios);
            estadisticas.put("naturales", usuariosNaturales);
            estadisticas.put("juridicos", usuariosJuridicos);
        } else {
            // Si no se especifica mes, mostrar todos los usuarios
            Long usuariosNaturales = usuarioRepository.countByTipoUsuario_Id(2);
            Long usuariosJuridicos = usuarioRepository.countByTipoUsuario_Id(3);
            Long totalUsuarios = usuariosNaturales + usuariosJuridicos;
            
            estadisticas.put("total", totalUsuarios);
            estadisticas.put("naturales", usuariosNaturales);
            estadisticas.put("juridicos", usuariosJuridicos);
        }
        
        return estadisticas;
    }
    @GetMapping("/exportar-excel")
    public void exportarExcel(@RequestParam(required = false) Integer mes, HttpServletResponse response) throws IOException {
        // Configurar la respuesta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "reporte_clientes_" + LocalDate.now() + ".xlsx";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        List<Usuario> usuariosNaturales;
        List<Usuario> usuariosJuridicos;
        
        if (mes != null && mes >= 1 && mes <= 12) {
            // Si se especifica un mes, filtrar por mes del año actual
            Integer anioActual = LocalDate.now().getYear();
            usuariosNaturales = usuarioRepository.findByTipoUsuarioAndMes(2, mes, anioActual);
            usuariosJuridicos = usuarioRepository.findByTipoUsuarioAndMes(3, mes, anioActual);
        } else {
            // Si no se especifica mes, obtener todos los usuarios
            usuariosNaturales = usuarioRepository.findByTipoUsuario_Id(2);
            usuariosJuridicos = usuarioRepository.findByTipoUsuario_Id(3);
        }
        // Crear el libro de Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clientes");
            // Crear estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            // Crear fila de encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Documento", "Nombre", "Primer Apellido", "Segundo Apellido", "Dirección", "Teléfono", "Perfil", "Ciudad", "Fecha Registro"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            int rowNum = 1;
            // Agregar usuarios naturales
            for (Usuario usuario : usuariosNaturales) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(usuario.getDocumento());
                row.createCell(1).setCellValue(usuario.getNombre());
                row.createCell(2).setCellValue(usuario.getPrimerApellido() != null ? usuario.getPrimerApellido() : "");
                row.createCell(3).setCellValue(usuario.getSegundoApellido() != null ? usuario.getSegundoApellido() : "");
                row.createCell(4).setCellValue(usuario.getDireccion() != null ? usuario.getDireccion() : "");
                row.createCell(5).setCellValue(usuario.getTelefono() != null ? usuario.getTelefono().toString() : "");
                row.createCell(6).setCellValue("Natural");
                row.createCell(7).setCellValue(usuario.getCiudad() != null ? usuario.getCiudad().getNombre() : "");
                row.createCell(8).setCellValue(usuario.getFechaRegistro() != null ? usuario.getFechaRegistro().toString() : "");
            }
            // Agregar usuarios jurídicos
            for (Usuario usuario : usuariosJuridicos) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(usuario.getDocumento());
                row.createCell(1).setCellValue(usuario.getNombre());
                row.createCell(2).setCellValue(""); // Los jurídicos no tienen primer apellido
                row.createCell(3).setCellValue(""); // Los jurídicos no tienen segundo apellido
                row.createCell(4).setCellValue(usuario.getDireccion() != null ? usuario.getDireccion() : "");
                row.createCell(5).setCellValue(usuario.getTelefono() != null ? usuario.getTelefono().toString() : "");
                row.createCell(6).setCellValue("Jurídico");
                row.createCell(7).setCellValue(usuario.getCiudad() != null ? usuario.getCiudad().getNombre() : "");
                row.createCell(8).setCellValue(usuario.getFechaRegistro() != null ? usuario.getFechaRegistro().toString() : "");
            }
            // Ajustar el ancho de las columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // Escribir el archivo
            workbook.write(response.getOutputStream());
        }
    }

    // ========== MÉTODOS PARA REPORTE DE ÓRDENES ==========

    @GetMapping("/estadisticas-ordenes")
    @ResponseBody
    public Map<String, Long> obtenerEstadisticasOrdenes(@RequestParam(required = false) Integer mes) {
        Map<String, Long> estadisticas = new HashMap<>();

        if (mes != null && mes >= 1 && mes <= 12) {
            // Si se especifica un mes, filtrar por mes del año actual
            Integer anioActual = LocalDate.now().getYear();

            // Obtener órdenes del mes especificado
            Long totalOrdenes = ordenRepository.countByMes(mes, anioActual);

            estadisticas.put("total", totalOrdenes);
        } else {
            // Si no se especifica mes, mostrar todas las órdenes
            Long totalOrdenes = ordenRepository.count();

            estadisticas.put("total", totalOrdenes);
        }

        return estadisticas;
    }

    @GetMapping("/exportar-zip-ordenes")
    public void exportarZipOrdenes(@RequestParam(required = false) Integer mes, HttpServletResponse response) throws IOException {
        // Configurar la respuesta HTTP
        response.setContentType("application/zip");
        String fileName = "ordenes_" + LocalDate.now() + ".zip";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        List<Orden> ordenes;

        if (mes != null && mes >= 1 && mes <= 12) {
            // Si se especifica un mes, filtrar por mes del año actual
            Integer anioActual = LocalDate.now().getYear();
            ordenes = ordenRepository.findByMes(mes, anioActual);
        } else {
            // Si no se especifica mes, obtener todas las órdenes
            ordenes = ordenRepository.findAll();
        }

        // Crear el archivo ZIP
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            for (Orden orden : ordenes) {
                try {
                    // Generar PDF para cada orden
                    byte[] pdfBytes = ordenService.generarOrdenPDF(orden.getId());

                    // Crear entrada en el ZIP
                    String entryName = "orden_" + orden.getId() + "_" + orden.getFechaOrden() + ".pdf";
                    ZipEntry zipEntry = new ZipEntry(entryName);
                    zipOut.putNextEntry(zipEntry);

                    // Escribir el PDF al ZIP
                    zipOut.write(pdfBytes);
                    zipOut.closeEntry();
                } catch (Exception e) {
                    // Si hay error con una orden específica, continuar con las demás
                    System.err.println("Error generando PDF para orden " + orden.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    // ========== MÉTODOS PARA REPORTE DE PRODUCTOS MÁS VENDIDOS ==========

    @GetMapping("/estadisticas-productos-vendidos")
    @ResponseBody
    public Map<String, Long> obtenerEstadisticasProductosVendidos(@RequestParam(required = false) Integer mes) {
        Map<String, Long> estadisticas = new HashMap<>();

        if (mes != null && mes >= 1 && mes <= 12) {
            // Si se especifica un mes, filtrar por mes del año actual
            Integer anioActual = LocalDate.now().getYear();

            // Obtener total de productos vendidos del mes especificado
            Long totalProductosVendidos = ordenRepository.countTotalProductosVendidosByMes(mes, anioActual);

            estadisticas.put("total", totalProductosVendidos != null ? totalProductosVendidos : 0L);
        } else {
            // Si no se especifica mes, mostrar todos los productos vendidos
            Long totalProductosVendidos = ordenRepository.countTotalProductosVendidos();

            estadisticas.put("total", totalProductosVendidos != null ? totalProductosVendidos : 0L);
        }

        return estadisticas;
    }

    @GetMapping("/exportar-excel-productos-vendidos")
    public void exportarExcelProductosVendidos(@RequestParam(required = false) Integer mes, HttpServletResponse response) throws IOException {
        // Configurar la respuesta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "reporte_productos_vendidos_" + LocalDate.now() + ".xlsx";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        List<Object[]> productosVendidos;

        if (mes != null && mes >= 1 && mes <= 12) {
            // Si se especifica un mes, filtrar por mes del año actual
            Integer anioActual = LocalDate.now().getYear();
            productosVendidos = ordenRepository.findProductosMasVendidosByMes(mes, anioActual);
        } else {
            // Si no se especifica mes, obtener todos los productos vendidos
            productosVendidos = ordenRepository.findProductosMasVendidos();
        }

        // Crear el archivo Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos Más Vendidos");

            // Crear estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Crear fila de encabezado
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Posición", "Nombre del Producto", "Cantidad Vendida"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Llenar datos de productos
            int rowNum = 1;
            int posicion = 1;
            for (Object[] productoData : productosVendidos) {
                Row row = sheet.createRow(rowNum++);

                // Posición
                row.createCell(0).setCellValue(posicion++);

                // Nombre del producto
                row.createCell(1).setCellValue(productoData[0] != null ? productoData[0].toString() : "");

                // Cantidad vendida
                Long cantidad = productoData[1] != null ? ((Number) productoData[1]).longValue() : 0L;
                row.createCell(2).setCellValue(cantidad);
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