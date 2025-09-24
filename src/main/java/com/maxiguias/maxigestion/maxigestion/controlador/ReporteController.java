package com.maxiguias.maxigestion.maxigestion.controlador;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;
import jakarta.servlet.http.HttpServletResponse;
@Controller
@RequestMapping("/reportes")
public class ReporteController {
    @Autowired
    private UsuarioRepository usuarioRepository;
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
            String[] headers = {"Documento", "Nombre", "Primer Apellido", "Segundo Apellido", "Dirección", "Teléfono", "Perfil", "Fecha Registro"};
            
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
                row.createCell(7).setCellValue(usuario.getFechaRegistro() != null ? usuario.getFechaRegistro().toString() : "");
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
                row.createCell(7).setCellValue(usuario.getFechaRegistro() != null ? usuario.getFechaRegistro().toString() : "");
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
