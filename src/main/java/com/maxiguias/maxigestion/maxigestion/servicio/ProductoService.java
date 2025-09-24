package com.maxiguias.maxigestion.maxigestion.servicio;


import java.util.List;
import java.util.Optional;


import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.Producto;
import com.maxiguias.maxigestion.maxigestion.repositorio.ProductoRepository;

@Service
public class ProductoService {
    
    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> buscarPorCodigoONombre(String keyword) {
        return productoRepository.findByIdProductoOrNombreGuia(keyword);
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardarProducto(Producto producto) {
        try {
            // Validación: ID obligatorio y mayor a 0
            if (producto.getId() == null || producto.getId() <= 0) {
                throw new IllegalArgumentException("El código del producto es obligatorio y debe ser mayor a 0");
            }

            // Validación: ID duplicado
            if (productoRepository.existsById(producto.getId())) {
                throw new IllegalArgumentException("No se puede crear el producto, el código ingresado ya existe");

            }

            // Relacionar terminados con el producto
            if (producto.getTerminados() != null) {
                producto.getTerminados().forEach(t -> t.setProducto(producto));
            }

            return productoRepository.save(producto);

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el producto: " + e.getMessage(), e);
        }
    }

    public Producto actualizarProducto(Long id, Producto producto) {
        // Validar que exista antes de actualizar
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Actualizamos solo los datos editables
        existente.setNombre(producto.getNombre());
        existente.setImagen(producto.getImagen());
        existente.setCantidadDisponible(producto.getCantidadDisponible());

        // Reemplazar terminados correctamente
        existente.getTerminados().clear();
        if (producto.getTerminados() != null) {
            producto.getTerminados().forEach(t -> {
                t.setProducto(existente);
                existente.getTerminados().add(t);
            });
        }

        return productoRepository.save(existente);
    }

    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        productoRepository.delete(producto);
    }

}

