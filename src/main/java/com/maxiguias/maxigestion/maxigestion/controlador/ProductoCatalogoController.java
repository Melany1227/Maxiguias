package com.maxiguias.maxigestion.maxigestion.controlador;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.maxiguias.maxigestion.maxigestion.dto.ProductoCatalogoDTO;
import com.maxiguias.maxigestion.maxigestion.servicio.ProductoCatalogoService;

import jakarta.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/catalogo")
public class ProductoCatalogoController {
    
    @Autowired
    private ProductoCatalogoService productoCatalogoService;
    
    @GetMapping("/productos")
    public String mostrarInformeProductos(Model model) {
        List<ProductoCatalogoDTO> productos = productoCatalogoService.obtenerProductosCatalogo();
        Integer totalProductos = productoCatalogoService.calcularTotalProductos();
        BigDecimal precioPromedio = productoCatalogoService.calcularPrecioPromedio();
        
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("precioPromedio", precioPromedio);
        model.addAttribute("busqueda", "");
        
        return "catalogo/informe-productos";
    }
    
    @PostMapping("/productos/buscar")
    public String buscarProductos(@RequestParam(value = "busqueda", required = false) String busqueda, 
                                  Model model) {
        List<ProductoCatalogoDTO> productos;
        
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            productos = productoCatalogoService.buscarProductos(busqueda);
        } else {
            productos = productoCatalogoService.obtenerProductosCatalogo();
        }
        
        Integer totalProductos = productos.size();
        BigDecimal precioPromedio = BigDecimal.ZERO;
        
        if (!productos.isEmpty()) {
            BigDecimal suma = productos.stream()
                    .map(ProductoCatalogoDTO::getPrecio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            precioPromedio = suma.divide(new BigDecimal(productos.size()), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        model.addAttribute("productos", productos);
        model.addAttribute("totalProductos", totalProductos);
        model.addAttribute("precioPromedio", precioPromedio);
        model.addAttribute("busqueda", busqueda);
        
        return "catalogo/informe-productos";
    }
    
    @GetMapping("/productos/exportar/excel")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=productos_catalogo.xlsx");
        
        List<ProductoCatalogoDTO> productos = productoCatalogoService.obtenerProductosCatalogo();
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");
        
        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Nombre");
        headerRow.createCell(2).setCellValue("Cantidad");
        headerRow.createCell(3).setCellValue("Precio");
        
        // Estilo para encabezados
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        for (Cell cell : headerRow) {
            cell.setCellStyle(headerStyle);
        }
        
        // Agregar datos
        int rowNum = 1;
        for (ProductoCatalogoDTO producto : productos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(producto.getId());
            row.createCell(1).setCellValue(producto.getNombre());
            row.createCell(2).setCellValue(producto.getCantidad());
            row.createCell(3).setCellValue(producto.getPrecio().doubleValue());
        }
        
        // Autoajustar columnas
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    
    @GetMapping("/productos/exportar/pdf")
    public String exportarPDF() {
        // Implementar exportación PDF si es necesario
        return "redirect:/catalogo/productos";
    }
    
    @GetMapping("/productos/cerrar")
    public String cerrarInforme() {
        return "redirect:/dashboard";  // O la ruta que uses para tu página principal
    }
}
