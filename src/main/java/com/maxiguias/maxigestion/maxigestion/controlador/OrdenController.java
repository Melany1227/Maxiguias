package com.maxiguias.maxigestion.maxigestion.controlador;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

import com.maxiguias.maxigestion.maxigestion.modelo.DetalleOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.EstadoOrden;
import com.maxiguias.maxigestion.maxigestion.modelo.Orden;
import com.maxiguias.maxigestion.maxigestion.modelo.Producto;
import com.maxiguias.maxigestion.maxigestion.modelo.Terminado;
import com.maxiguias.maxigestion.maxigestion.modelo.Usuario;
import com.maxiguias.maxigestion.maxigestion.repositorio.CiudadRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.EmpresaRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.ProductoRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.TerminadoRepository;
import com.maxiguias.maxigestion.maxigestion.repositorio.UsuarioRepository;
import com.maxiguias.maxigestion.maxigestion.servicio.OrdenService;
import com.maxiguias.maxigestion.maxigestion.servicio.TerminadoService;

@Controller
@RequestMapping("/ordenes")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    @Autowired
    private TerminadoService terminadoService;

    @Autowired
    private TerminadoRepository terminadoRepository;

    @GetMapping("/nueva")
    public String mostrarFormulario(Model model, HttpSession session) {
        // Obtener usuario logueado
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        
        model.addAttribute("orden", new Orden());
        model.addAttribute("detalles", new ArrayList<DetalleOrden>());
        model.addAttribute("empresa", empresaRepository.findAll().get(0)); 
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("ciudades", ciudadRepository.findAll());
        model.addAttribute("terminados", terminadoService.obtenerTodosLosTerminados()); 
        
        // Determinar si el usuario es administrador o cliente jurídico
        boolean esAdministrador = false;
        boolean esClienteJuridico = false;
        
        if (usuarioLogueado != null) {
            // Es administrador si tiene rol "ADMINISTRADOR"
            if (usuarioLogueado.getPerfil() != null && 
                usuarioLogueado.getPerfil().getRol() != null &&
                "ADMINISTRADOR".equals(usuarioLogueado.getPerfil().getRol().getNombreRol())) {
                esAdministrador = true;
            }
            
            // Es cliente jurídico si su tipo de usuario es "JURIDICO"
            if (usuarioLogueado.getTipoUsuario() != null &&
                "JURIDICO".equals(usuarioLogueado.getTipoUsuario().getNombre())) {
                esClienteJuridico = true;
            }
        }
        
        model.addAttribute("esAdministrador", esAdministrador);
        model.addAttribute("esClienteJuridico", esClienteJuridico);
        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("editMode", false); // Para nueva orden, editMode es false

        return "orden-form";
    }

    @GetMapping("/buscar-usuarios")
    @ResponseBody
    public List<Usuario> buscarUsuarios(@RequestParam String termino, HttpSession session) {
        // Verificar que el usuario logueado sea administrador
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        
        if (usuarioLogueado == null || 
            usuarioLogueado.getPerfil() == null || 
            usuarioLogueado.getPerfil().getRol() == null ||
            !"ADMINISTRADOR".equals(usuarioLogueado.getPerfil().getRol().getNombreRol())) {
            // Solo los administradores pueden buscar usuarios
            return new ArrayList<>();
        }
        try {
            if (termino == null || termino.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            // Usar la nueva consulta optimizada
            List<Usuario> usuarios = usuarioRepository.buscarUsuariosPorTermino(
                Arrays.asList("NATURAL", "JURIDICO"), 
                termino.trim()
            );
            
            // Limitar resultados para mejor rendimiento
            return usuarios.stream()
                .limit(10)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback a la implementación anterior si hay error
            try {
                List<Usuario> usuarios = usuarioRepository.findByTipoUsuario_NombreIn(Arrays.asList("NATURAL", "JURIDICO"));
                String terminoBusqueda = termino.trim().toLowerCase();
                
                return usuarios.stream()
                    .filter(usuario -> {
                        if (usuario == null) return false;
                        
                        String nombre = usuario.getNombre() != null ? usuario.getNombre().toLowerCase() : "";
                        String primerApellido = usuario.getPrimerApellido() != null ? usuario.getPrimerApellido().toLowerCase() : "";
                        String nombreCompleto = (nombre + " " + primerApellido).trim();
                        String documento = usuario.getDocumento() != null ? usuario.getDocumento().toString() : "";
                        
                        return nombreCompleto.contains(terminoBusqueda) ||
                               nombre.contains(terminoBusqueda) ||
                               primerApellido.contains(terminoBusqueda) ||
                               documento.contains(termino.trim());
                    })
                    .limit(10)
                    .collect(Collectors.toList());
            } catch (Exception fallbackError) {
                fallbackError.printStackTrace();
                return new ArrayList<>();
            }
        }
    }


    @PostMapping("/guardar")
    public String guardarOrden(
            @ModelAttribute Orden orden,
            @RequestParam("productoId") List<Long> productosId,
            @RequestParam("terminadoId") List<Long> terminadosId,
            @RequestParam("cantidad") List<Integer> cantidades,
            @RequestParam("valor") List<BigDecimal> valores,
            @RequestParam("descripcion") List<String> descripciones,
            HttpSession session
    ) {
        // Obtener usuario logueado
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        
        if (usuarioLogueado == null) {
            return "redirect:/login?error=Session expired";
        }
        
        // Validación de seguridad: Si es cliente jurídico, solo puede crear órdenes para sí mismo
        boolean esAdministrador = usuarioLogueado.getPerfil() != null && 
                                usuarioLogueado.getPerfil().getRol() != null &&
                                "ADMINISTRADOR".equals(usuarioLogueado.getPerfil().getRol().getNombreRol());
        
        boolean esClienteJuridico = usuarioLogueado.getTipoUsuario() != null &&
                                  "JURIDICO".equals(usuarioLogueado.getTipoUsuario().getNombre());
        
        if (esClienteJuridico && !esAdministrador) {
            // Si es cliente jurídico (no administrador), forzar la orden para él mismo
            orden.setUsuario(usuarioLogueado);
        }

        // Validar que la fecha de entrega sea mayor a la fecha actual
        if (orden.getFechaEntrega() != null && orden.getFechaEntrega().isBefore(LocalDateTime.now())) {
            return "redirect:/ordenes/nueva?error=La fecha de entrega debe ser mayor a la fecha actual";
        }

        Orden ordenGuardada = ordenService.guardarOrden(orden);

        for (int i = 0; i < terminadosId.size(); i++) {
            DetalleOrden detalle = new DetalleOrden();
            Terminado terminado = terminadoRepository.findById(terminadosId.get(i))
                .orElseThrow(() -> new RuntimeException("Terminado no encontrado"));

            detalle.setOrden(ordenGuardada); 
            detalle.setTerminado(terminado);       
            detalle.setCantidad(cantidades.get(i));
            detalle.setValor(valores.get(i));
            detalle.setDescripcion(descripciones.get(i));
            ordenService.guardarDetalles(detalle);
        }

        return "redirect:/ordenes?mensaje=Orden creada satisfactoriamente";
    }

    @GetMapping
    public String listarOrdenes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filtroCliente,
            @RequestParam(required = false) String mensaje,
            @RequestParam(required = false) String error,
            Model model,
            HttpSession session) {
        
        // Obtener usuario logueado
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        
        if (usuarioLogueado == null) {
            return "redirect:/login?error=Session expired";
        }
        
        // Verificar si es usuario jurídico
        boolean esClienteJuridico = usuarioLogueado.getTipoUsuario() != null &&
                                  "JURIDICO".equals(usuarioLogueado.getTipoUsuario().getNombre());
        
        boolean esAdministrador = usuarioLogueado.getPerfil() != null && 
                                usuarioLogueado.getPerfil().getRol() != null &&
                                "ADMINISTRADOR".equals(usuarioLogueado.getPerfil().getRol().getNombreRol());
        
        // Ordenamiento por fecha de orden descendente (más recientes primero)
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaOrden").descending());
        Page<Orden> ordenesPage;
        
        // Si es cliente jurídico (no administrador), solo mostrar sus órdenes
        if (esClienteJuridico && !esAdministrador) {
            Long usuarioDocumento = usuarioLogueado.getDocumento();
            
            if (filtroCliente != null && !filtroCliente.trim().isEmpty()) {
                // Buscar dentro de las órdenes del usuario
                ordenesPage = ordenService.buscarOrdenesPorClienteYUsuario(filtroCliente.trim(), usuarioDocumento, pageable);
            } else {
                // Mostrar solo las órdenes del usuario
                ordenesPage = ordenService.obtenerOrdenesPorUsuario(usuarioDocumento, pageable);
            }
        } else {
            // Para administradores, mostrar todas las órdenes (comportamiento original)
            if (filtroCliente != null && !filtroCliente.trim().isEmpty()) {
                try {
                    // Intentar convertir a número para buscar por documento
                    Long documento = Long.parseLong(filtroCliente.trim());
                    ordenesPage = ordenService.filtrarOrdenesPorClienteDocumento(documento, pageable);
                } catch (NumberFormatException e) {
                    // Si no es número, buscar por nombre
                    ordenesPage = ordenService.buscarOrdenesPorCliente(filtroCliente.trim(), pageable);
                }
            } else {
                ordenesPage = ordenService.obtenerOrdenesPaginadas(pageable);
            }
        }
        
        model.addAttribute("ordenes", ordenesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordenesPage.getTotalPages());
        model.addAttribute("totalElements", ordenesPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("filtroCliente", filtroCliente);
        
        // Agregar mensajes de confirmación
        if (mensaje != null) {
            model.addAttribute("mensaje", mensaje);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        
        return "listar_ordenes"; 
    }

    @GetMapping("/verMas/{id}")
    public String verDetalleOrden(@PathVariable("id") Long id, Model model, HttpSession session) {
        Orden orden = ordenService.obtenerOrdenPorId(id);
        if (orden == null) {
            return "redirect:/ordenes?error=Orden no encontrada";
        }
        
        // Obtener usuario logueado para validación de permisos
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        
        if (usuarioLogueado == null) {
            return "redirect:/login?error=Session expired";
        }
        
        // Verificar permisos para usuarios jurídicos
        boolean esClienteJuridico = usuarioLogueado.getTipoUsuario() != null &&
                                  "JURIDICO".equals(usuarioLogueado.getTipoUsuario().getNombre());
        
        boolean esAdministrador = usuarioLogueado.getPerfil() != null && 
                                usuarioLogueado.getPerfil().getRol() != null &&
                                "ADMINISTRADOR".equals(usuarioLogueado.getPerfil().getRol().getNombreRol());
        
        // Control de acceso: cliente jurídico solo puede ver sus propias órdenes
        if (esClienteJuridico && !esAdministrador) {
            if (!orden.getUsuario().getDocumento().equals(usuarioLogueado.getDocumento())) {
                return "redirect:/ordenes?error=No tiene permisos para ver esta orden";
            }
        }

        model.addAttribute("orden", orden);
        model.addAttribute("detalles", orden.getDetalles());
        return "orden-detalle";
    }

    @GetMapping("/pdf/{id}")
    public String descargarOrdenPDF(@PathVariable("id") Long id, HttpSession session, Model model) {
        try {
            Orden orden = ordenService.obtenerOrdenPorId(id);
            if (orden == null) {
                return listarOrdenes(0, 10, null, null, "Orden no encontrada", model, session);
            }
            
            // Obtener usuario logueado para validación de permisos
            Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
            
            if (usuarioLogueado == null) {
                return "redirect:/login?error=Session expired";
            }
            
            // Verificar permisos para usuarios jurídicos
            boolean esClienteJuridico = usuarioLogueado.getTipoUsuario() != null &&
                                      "JURIDICO".equals(usuarioLogueado.getTipoUsuario().getNombre());
            
            boolean esAdministrador = usuarioLogueado.getPerfil() != null && 
                                    usuarioLogueado.getPerfil().getRol() != null &&
                                    "ADMINISTRADOR".equals(usuarioLogueado.getPerfil().getRol().getNombreRol());
            
            // Control de acceso: cliente jurídico solo puede descargar PDF de sus propias órdenes
            if (esClienteJuridico && !esAdministrador) {
                if (!orden.getUsuario().getDocumento().equals(usuarioLogueado.getDocumento())) {
                    return listarOrdenes(0, 10, null, null, "No tiene permisos para descargar esta orden", model, session);
                }
            }
            
            // Validar que la orden esté en estado FINALIZADA o FACTURADA para generar PDF
            if (orden.getEstado() != EstadoOrden.FINALIZADA && orden.getEstado() != EstadoOrden.FACTURADA) {
                return listarOrdenes(0, 10, null, null, "Solo se puede generar PDF de órdenes finalizadas o facturadas", model, session);
            }
            
            byte[] pdfBytes = ordenService.generarOrdenPDF(id);
            
            // Si llegamos aquí, el PDF se generó exitosamente
            // Redirigir a un endpoint que descargue el archivo
            return "redirect:/ordenes/descargar-pdf/" + id;
            
        } catch (RuntimeException e) {
            // Si es el error específico de representante, mostrar mensaje específico
            if (e.getMessage() != null && e.getMessage().contains("Es necesario configurar un usuario con perfil de REPRESENTANTE")) {
                return listarOrdenes(0, 10, null, null, "Es necesario configurar un usuario con perfil de REPRESENTANTE en el sistema", model, session);
            }
            // Para otros errores RuntimeException
            return listarOrdenes(0, 10, null, null, "Error interno al generar el PDF", model, session);
        } catch (Exception e) {
            // Log del error general
            e.printStackTrace();
            return listarOrdenes(0, 10, null, null, "Error inesperado al generar el PDF", model, session);
        }
    }
    
    @GetMapping("/descargar-pdf/{id}")
    public ResponseEntity<byte[]> descargarPDF(@PathVariable("id") Long id, HttpSession session) {
        try {
            Orden orden = ordenService.obtenerOrdenPorId(id);
            if (orden == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            // Verificar permisos básicos
            Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
            if (usuarioLogueado == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            
            byte[] pdfBytes = ordenService.generarOrdenPDF(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "orden_" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id, Model model, HttpSession session) {
        Orden orden = ordenService.obtenerOrdenPorId(id);
        if (orden == null) {
            return "redirect:/ordenes?error=Orden no encontrada";
        }
        
        // Validar estados que permiten edición
        if (orden.getEstado() != EstadoOrden.PENDIENTE && 
            orden.getEstado() != EstadoOrden.EN_PROCESO && 
            orden.getEstado() != EstadoOrden.FACTURADA) {
            return "redirect:/ordenes?error=No se puede editar una orden en este estado";
        }
        
        // Obtener usuario logueado para la lógica de permisos
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        
        if (usuarioLogueado == null) {
            return "redirect:/login?error=Session expired";
        }
        
        // Determinar si el usuario es administrador o cliente jurídico
        boolean esAdministrador = false;
        boolean esClienteJuridico = false;
        
        if (usuarioLogueado != null) {
            // Es administrador si tiene rol "ADMINISTRADOR"
            if (usuarioLogueado.getPerfil() != null && 
                usuarioLogueado.getPerfil().getRol() != null &&
                "ADMINISTRADOR".equals(usuarioLogueado.getPerfil().getRol().getNombreRol())) {
                esAdministrador = true;
            }
            
            // Es cliente jurídico si su tipo de usuario es "JURIDICO"
            if (usuarioLogueado.getTipoUsuario() != null &&
                "JURIDICO".equals(usuarioLogueado.getTipoUsuario().getNombre())) {
                esClienteJuridico = true;
            }
        }
        
        // Control de acceso: cliente jurídico solo puede editar sus propias órdenes
        if (esClienteJuridico && !esAdministrador) {
            if (!orden.getUsuario().getDocumento().equals(usuarioLogueado.getDocumento())) {
                return "redirect:/ordenes?error=No tiene permisos para editar esta orden";
            }
        }
        
        model.addAttribute("orden", orden);
        model.addAttribute("detalles", orden.getDetalles());
        model.addAttribute("empresa", empresaRepository.findAll().get(0)); 
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("ciudades", ciudadRepository.findAll());
        model.addAttribute("terminados", terminadoService.obtenerTodosLosTerminados());
        model.addAttribute("editMode", true);
        model.addAttribute("esAdministrador", esAdministrador);
        model.addAttribute("esClienteJuridico", esClienteJuridico);
        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("esOrdenFacturada", orden.getEstado() == EstadoOrden.FACTURADA);
        
        return "orden-form";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarOrden(
            @PathVariable("id") Long id,
            @ModelAttribute Orden ordenActualizada,
            @RequestParam("productoId") List<Long> productosId,
            @RequestParam("terminadoId") List<Long> terminadosId,
            @RequestParam("cantidad") List<Integer> cantidades,
            @RequestParam("valor") List<BigDecimal> valores,
            @RequestParam("descripcion") List<String> descripciones,
            HttpSession session
    ) {
        Orden orden = ordenService.obtenerOrdenPorId(id);
        if (orden == null) {
            return "redirect:/ordenes?error=Orden no encontrada";
        }
        
        // Validar estados que permiten actualización
        if (orden.getEstado() != EstadoOrden.PENDIENTE && 
            orden.getEstado() != EstadoOrden.EN_PROCESO && 
            orden.getEstado() != EstadoOrden.FACTURADA) {
            return "redirect:/ordenes?error=No se puede actualizar una orden en este estado";
        }
        
        // Obtener usuario logueado para validación de permisos
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        
        if (usuarioLogueado == null) {
            return "redirect:/login?error=Session expired";
        }
        
        // Verificar permisos para usuarios jurídicos
        boolean esClienteJuridico = usuarioLogueado.getTipoUsuario() != null &&
                                  "JURIDICO".equals(usuarioLogueado.getTipoUsuario().getNombre());
        
        boolean esAdministrador = usuarioLogueado.getPerfil() != null && 
                                usuarioLogueado.getPerfil().getRol() != null &&
                                "ADMINISTRADOR".equals(usuarioLogueado.getPerfil().getRol().getNombreRol());
        
        // Control de acceso: cliente jurídico solo puede actualizar sus propias órdenes
        if (esClienteJuridico && !esAdministrador) {
            if (!orden.getUsuario().getDocumento().equals(usuarioLogueado.getDocumento())) {
                return "redirect:/ordenes?error=No tiene permisos para actualizar esta orden";
            }
        }

        // Determinar si la orden está facturada (edición restringida)
        boolean esOrdenFacturada = orden.getEstado() == EstadoOrden.FACTURADA;
        
        if (esOrdenFacturada) {
            // Para órdenes facturadas, solo se puede cambiar el estado
            // Validar que el nuevo estado sea CANCELADA o FINALIZADA
            if (ordenActualizada.getEstado() != EstadoOrden.CANCELADA && 
                ordenActualizada.getEstado() != EstadoOrden.FINALIZADA) {
                return "redirect:/ordenes/editar/" + id + "?error=Las órdenes facturadas solo pueden cambiarse a Cancelada o Finalizada";
            }
            
            // Solo actualizar el estado
            orden.setEstado(ordenActualizada.getEstado());
            ordenService.guardarOrden(orden);
            
        } else {
            // Para órdenes PENDIENTE y EN_PROCESO, edición completa
            
            // Validar que la fecha de entrega sea mayor a la fecha actual
            if (ordenActualizada.getFechaEntrega() != null && ordenActualizada.getFechaEntrega().isBefore(LocalDateTime.now())) {
                return "redirect:/ordenes/editar/" + id + "?error=La fecha de entrega debe ser mayor a la fecha actual";
            }
            
            orden.setFechaEntrega(ordenActualizada.getFechaEntrega());
            orden.setDescripcionVenta(ordenActualizada.getDescripcionVenta());
            orden.setTotalFactura(ordenActualizada.getTotalFactura());
            orden.setEstado(ordenActualizada.getEstado());
            
            Orden ordenGuardada = ordenService.guardarOrden(orden);
            
            ordenService.eliminarDetallesOrden(id);
            
            for (int i = 0; i < terminadosId.size(); i++) {
                DetalleOrden detalle = new DetalleOrden();
                Terminado terminado = terminadoRepository.findById(terminadosId.get(i))
                    .orElseThrow(() -> new RuntimeException("Terminado no encontrado"));

                detalle.setOrden(ordenGuardada); 
                detalle.setTerminado(terminado);       
                detalle.setCantidad(cantidades.get(i));
                detalle.setValor(valores.get(i));
                detalle.setDescripcion(descripciones.get(i));
                ordenService.guardarDetalles(detalle);
            }
        }

        return "redirect:/ordenes?mensaje=Orden actualizada satisfactoriamente";
    }

}