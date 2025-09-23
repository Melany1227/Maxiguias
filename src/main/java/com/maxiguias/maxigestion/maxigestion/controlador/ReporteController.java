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
        
        // Obtener usuarios naturales (tipo_usuario = 2)
        Long usuariosNaturales = usuarioRepository.countByTipoUsuario_Id(2);

        // Obtener usuarios jurídicos (tipo_usuario = 3)
        Long usuariosJuridicos = usuarioRepository.countByTipoUsuario_Id(3);

        // Total de usuarios (naturales + jurídicos)
        Long totalUsuarios = usuariosNaturales + usuariosJuridicos;

        estadisticas.put("total", totalUsuarios);
        estadisticas.put("naturales", usuariosNaturales);
        estadisticas.put("juridicos", usuariosJuridicos);

        return estadisticas;
    }

    @GetMapping("/exportar-excel")
    public void exportarExcel(@RequestParam(required = false) Integer mes, HttpServletResponse response) throws IOException {
        // Configurar la respuesta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = "reporte_clientes_" + LocalDate.now() + ".xlsx";
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        // Obtener usuarios naturales y jurídicos
        List<Usuario> usuariosNaturales = usuarioRepository.findByTipoUsuario_Id(2);
        List<Usuario> usuariosJuridicos = usuarioRepository.findByTipoUsuario_Id(3);

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
            String[] headers = {"Documento", "Nombre", "Primer Apellido", "Segundo Apellido", "Dirección", "Teléfono", "Perfil"};
            
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