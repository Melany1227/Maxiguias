package com.maxiguias.maxigestion.maxigestion.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.dto.ProductoCatalogoDTO;
import com.maxiguias.maxigestion.maxigestion.modelo.Producto;
import com.maxiguias.maxigestion.maxigestion.repositorio.ProductoRepository;

@Service
public class ProductoCatalogoService {
    
    @Autowired
    private ProductoRepository productoRepository;

    public List<ProductoCatalogoDTO> obtenerProductosCatalogo() {
        List<Object[]> resultados = productoRepository.obtenerProductosConEstadisticas();
        List<ProductoCatalogoDTO> productos = new ArrayList<>();
        
        for (Object[] resultado : resultados) {
            ProductoCatalogoDTO dto = new ProductoCatalogoDTO();
            dto.setId(((Number) resultado[0]).longValue());
            dto.setNombre((String) resultado[1]);
            dto.setCantidad(((Number) resultado[2]).intValue());
            dto.setPrecio(new BigDecimal(resultado[3].toString()));
            productos.add(dto);
        }
        
        return productos;
    }
    
    public List<ProductoCatalogoDTO> buscarProductos(String busqueda) {
        List<Producto> productos = productoRepository.buscarPorNombre(busqueda);
        List<ProductoCatalogoDTO> productosDTO = new ArrayList<>();
        
        for (Producto producto : productos) {
            ProductoCatalogoDTO dto = new ProductoCatalogoDTO();
            dto.setId(producto.getId());
            dto.setNombre(producto.getNombre());
            dto.setImagen(producto.getImagen());
            
            // Calcula cantidad total vendida
            int cantidadTotal = 0;
            BigDecimal sumaPrecios = BigDecimal.ZERO;
            int contador = 0;
            
            /*
            if (producto.getDetalleFacturas() != null) {
                for (DetalleFactura df : producto.getDetalleFacturas()) {
                    // Usa getCantidad() en lugar de getCantidadProducto()
                    if (df.getCantidad() != null) {
                        cantidadTotal += df.getCantidad();
                    }
                    // Usa getValor() en lugar de getValorProducto()
                    if (df.getValor() != null) {
                        sumaPrecios = sumaPrecios.add(df.getValor());
                        contador++;
                    }
                }
            } */
            
            dto.setCantidad(cantidadTotal);
            
            // Calcula precio promedio
            if (contador > 0) {
                BigDecimal precioPromedio = sumaPrecios.divide(
                    new BigDecimal(contador), 
                    2, 
                    RoundingMode.HALF_UP
                );
                dto.setPrecio(precioPromedio);
            } else {
                dto.setPrecio(BigDecimal.ZERO);
            }
            
            productosDTO.add(dto);
        }
        
        return productosDTO;
    }
    
    // Versión con streams si prefieres:
    public List<ProductoCatalogoDTO> buscarProductosConStreams(String busqueda) {
        List<Producto> productos = productoRepository.buscarPorNombre(busqueda);
        List<ProductoCatalogoDTO> productosDTO = new ArrayList<>();
        
        for (Producto producto : productos) {
            ProductoCatalogoDTO dto = new ProductoCatalogoDTO();
            dto.setId(producto.getId());
            dto.setNombre(producto.getNombre());
            dto.setImagen(producto.getImagen());
            /*
            if (producto.getDetalleFacturas() != null && !producto.getDetalleFacturas().isEmpty()) {
                // Calcula cantidad total vendida
                int cantidadTotal = producto.getDetalleFacturas().stream()
                        .mapToInt(df -> df.getCantidad() != null ? df.getCantidad() : 0)
                        .sum();
                dto.setCantidad(cantidadTotal);
                
                // Calcula precio promedio
                BigDecimal precioPromedio = producto.getDetalleFacturas().stream()
                        .map(df -> df.getValor() != null ? df.getValor() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                precioPromedio = precioPromedio.divide(
                    new BigDecimal(producto.getDetalleFacturas().size()), 
                    2, 
                    RoundingMode.HALF_UP
                ); 
                dto.setPrecio(precioPromedio);
            } else {*/
                dto.setCantidad(0);
                dto.setPrecio(BigDecimal.ZERO);
            //}
            
            productosDTO.add(dto);
        }
        
        return productosDTO;
    }
    
    public Integer calcularTotalProductos() {
        List<ProductoCatalogoDTO> productos = obtenerProductosCatalogo();
        return productos.size();
    }
    
    public BigDecimal calcularPrecioPromedio() {
        List<ProductoCatalogoDTO> productos = obtenerProductosCatalogo();
        if (productos.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal suma = productos.stream()
                .map(ProductoCatalogoDTO::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return suma.divide(new BigDecimal(productos.size()), 2, RoundingMode.HALF_UP);
    }
}
