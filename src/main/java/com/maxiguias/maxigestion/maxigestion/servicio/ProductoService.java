package com.maxiguias.maxigestion.maxigestion.servicio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.maxiguias.maxigestion.maxigestion.modelo.Producto;
import com.maxiguias.maxigestion.maxigestion.repositorio.ProductoRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.DetalleOrdenRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;

    public ProductoService(ProductoRepository productoRepository, DetalleOrdenRepository detalleOrdenRepository) {
        this.productoRepository = productoRepository;
        this.detalleOrdenRepository = detalleOrdenRepository;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardarProducto(Producto producto) {

        if (producto.getId() == null || producto.getId() <= 0) {
            throw new IllegalArgumentException("El código del producto es obligatorio y debe ser mayor a 0");
        }
        if (productoRepository.existsById(producto.getId())) {
            throw new IllegalArgumentException("No se puede crear el producto, el código ingresado ya existe");
        }

        // Relacionar terminados con el producto
        if (producto.getTerminados() != null) {
            producto.getTerminados().forEach(t -> t.setProducto(producto));
        }

        try {
            return productoRepository.save(producto);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar el producto: " + e.getMessage(), e);
        }
    }

    public Producto actualizarProducto(Long id, Producto producto, String terminadosEliminados) {
        // Validar que exista antes de actualizar
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Actualizamos solo los datos editables
        existente.setNombre(producto.getNombre());
        existente.setImagen(producto.getImagen());
        existente.setCantidadDisponible(producto.getCantidadDisponible());

        // Procesar terminados eliminados
        if (terminadosEliminados != null && !terminadosEliminados.isEmpty()) {
            String[] idsEliminados = terminadosEliminados.split(",");
            for (String idStr : idsEliminados) {
                try {
                    Long terminadoId = Long.parseLong(idStr.trim());
                    // Verificar si el terminado está en uso
                    if (detalleOrdenRepository.existsByTerminadoId(terminadoId)) {
                        throw new RuntimeException("No se puede eliminar el terminado porque está siendo usado en facturas. Por favor, revise las órdenes asociadas.");
                    }
                    // Eliminar el terminado de la lista
                    existente.getTerminados().removeIf(t -> t.getId() != null && t.getId().equals(terminadoId));
                } catch (NumberFormatException e) {
                    // Ignorar si el ID no es válido
                }
            }
        }

        // Actualizar terminados: solo actualizar los existentes
        if (producto.getTerminados() != null && !producto.getTerminados().isEmpty()) {
            for (var nuevoTerminado : producto.getTerminados()) {
                // Si el terminado tiene ID, es uno existente que debe actualizarse
                if (nuevoTerminado.getId() != null && nuevoTerminado.getId() > 0) {
                    // Buscar el terminado existente en la lista actual
                    var terminadoExistente = existente.getTerminados().stream()
                            .filter(t -> t.getId() != null && t.getId().equals(nuevoTerminado.getId()))
                            .findFirst();

                    if (terminadoExistente.isPresent()) {
                        // Actualizar solo los precios y ganancia, no el producto
                        var t = terminadoExistente.get();
                        t.setMedidaTerminadoProducto(nuevoTerminado.getMedidaTerminadoProducto());
                        t.setPrecioPublico(nuevoTerminado.getPrecioPublico());
                        t.setPrecioPorMayor(nuevoTerminado.getPrecioPorMayor());
                        t.setPrecioPorEncargo(nuevoTerminado.getPrecioPorEncargo());
                        t.setGananciaXMayor(nuevoTerminado.getGananciaXMayor());
                        t.setGananciaXEncargo(nuevoTerminado.getGananciaXEncargo());
                    }
                } else {
                    // Es un terminado nuevo (sin ID), agregarlo
                    nuevoTerminado.setProducto(existente);
                    existente.getTerminados().add(nuevoTerminado);
                }
            }
        }

        return productoRepository.save(existente);
    }

    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar si algún terminado del producto está siendo usado en facturas/órdenes
        if (producto.getTerminados() != null && !producto.getTerminados().isEmpty()) {
            for (var terminado : producto.getTerminados()) {
                if (detalleOrdenRepository.existsByTerminadoId(terminado.getId())) {
                    throw new RuntimeException("No se puede eliminar el producto \"" + producto.getNombre()
                        + "\" porque sus terminados están siendo usados en facturas. Por favor, elimine primero las facturas asociadas.");
                }
            }
        }

        productoRepository.delete(producto);
    }

    public Page<Producto> listarProductosPaginados(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productoRepository.findAll(pageable);
    }

    public Page<Producto> buscarPorCodigoONombrePaginado(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productoRepository.buscarPorCodigoONombre(keyword, pageable);
    }

}
