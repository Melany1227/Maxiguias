// tipoClienteGlobal está definido en orden-form.html

function esCombinacionDuplicada(productoId, terminadoId, filaActual) {
    const filas = document.querySelectorAll("#tablaDetalle tbody tr");
    
    for (let fila of filas) {
        if (fila === filaActual) continue;
        
        const prodSeleccionado = fila.querySelector("select[name='productoId']").value;
        const termSeleccionado = fila.querySelector("select[name='terminadoId']").value;
        
        if (prodSeleccionado === productoId && termSeleccionado === terminadoId) {
            return true;
        }
    }
    
    return false;
}

async function productoTieneTerminadosDisponibles(productoId, filaActual) {
    try {
        const response = await fetch(`/terminados/por-producto/${productoId}`);
        const terminados = await response.json();

        return terminados.some(terminado => 
            !esCombinacionDuplicada(productoId, terminado.id.toString(), filaActual)
        );
    } catch (error) {
        console.error("Error verificando terminados:", error);
        return true;
    }
}

async function actualizarOpcionesProducto(fila) {
    const productoSelect = fila.querySelector("select[name='productoId']");
    const productoActual = productoSelect.value;
    
    if (!window.opcionesProductosOriginales) {
        window.opcionesProductosOriginales = Array.from(productoSelect.options)
            .filter(option => option.value !== "")
            .map(option => ({
                value: option.value,
                text: option.textContent,
                dataNombre: option.getAttribute("data-nombre")
            }));
    }
    
    productoSelect.innerHTML = '<option value="">Seleccione un producto</option>';
    
    for (const opcion of window.opcionesProductosOriginales) {
        const tieneDisponibles = await productoTieneTerminadosDisponibles(opcion.value, fila);
        if (tieneDisponibles) {
            const option = document.createElement("option");
            option.value = opcion.value;
            option.textContent = opcion.text;
            option.setAttribute("data-nombre", opcion.dataNombre);
            productoSelect.appendChild(option);
        }
    }
    
    if (productoActual) {
        productoSelect.value = productoActual;
        if (!productoSelect.value) {
            const selectTerminado = fila.querySelector(".select-terminado");
            selectTerminado.innerHTML = "<option value=''>Seleccione un terminado</option>";
        }
    }
}

async function actualizarTodasLasOpcionesProducto() {
    const filas = document.querySelectorAll("#tablaDetalle tbody tr");
    for (const fila of filas) {
        await actualizarOpcionesProducto(fila);
    }
}

async function actualizarOpcionesProductoOtrasFilas(filaActual) {
    const filas = document.querySelectorAll("#tablaDetalle tbody tr");
    for (const fila of filas) {
        if (fila !== filaActual) {
            await actualizarOpcionesProducto(fila);
        }
    }
}

async function actualizarDisponibilidadProductos() {
    const filas = document.querySelectorAll("#tablaDetalle tbody tr");
    
    // Cache para evitar múltiples peticiones del mismo producto
    const terminadosCache = new Map();
    
    for (const fila of filas) {
        const productoSelect = fila.querySelector("select[name='productoId']");
        const productoActual = productoSelect.value;
        
        // Recorrer todas las opciones y verificar disponibilidad
        for (const option of productoSelect.options) {
            if (option.value === "") continue; // Skip placeholder option
            
            const productoId = option.value;
            
            // Usar cache para evitar peticiones repetidas
            if (!terminadosCache.has(productoId)) {
                try {
                    const response = await fetch(`/terminados/por-producto/${productoId}`);
                    const terminados = await response.json();
                    terminadosCache.set(productoId, terminados);
                } catch (error) {
                    console.error(`Error cargando terminados para producto ${productoId}:`, error);
                    terminadosCache.set(productoId, []);
                }
            }
            
            const terminados = terminadosCache.get(productoId);
            const tieneDisponibles = terminados.some(terminado => 
                !esCombinacionDuplicada(productoId, terminado.id.toString(), fila)
            );
            
            // Habilitar/deshabilitar la opción basado en disponibilidad
            option.disabled = !tieneDisponibles;
            
            // Si el producto actual se volvió no disponible, resetear
            if (productoId === productoActual && !tieneDisponibles) {
                productoSelect.value = "";
                const terminadoSelect = fila.querySelector("select[name='terminadoId']");
                terminadoSelect.innerHTML = "<option value=''>Seleccione un terminado</option>";
                actualizarDescripcion(fila);
                actualizarPrecio(fila);
            }
        }
    }
}

function actualizarClienteInfo() {
    if (window.usuarioSeleccionadoGlobal) {
        window.tipoClienteGlobal = window.usuarioSeleccionadoGlobal.tipoUsuario.nombre.toUpperCase();
        
        document.querySelectorAll("#tablaDetalle tbody tr").forEach(fila => {
            actualizarTextoTerminados(fila);
            actualizarPrecio(fila);
        });
        
        calcularTotalOrden();
    }
}

// Función para actualizar todos los textos cuando cambie el tipo de cliente globalmente
function actualizarTodosLosTextosTerminados() {
    console.log('🔄 Actualizando todos los textos de terminados para tipo:', window.tipoClienteGlobal);
    document.querySelectorAll("#tablaDetalle tbody tr").forEach(fila => {
        actualizarTextoTerminados(fila);
    });
}

async function cargarTerminadosParaFila(fila, productoId, terminadoSeleccionado = null) {
    const terminadoSelect = fila.querySelector("select[name='terminadoId']");
    
    try {
        terminadoSelect.innerHTML = "<option value=''>Cargando...</option>";
        
        const response = await fetch(`/terminados/por-producto/${productoId}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const terminados = await response.json();
        
        terminadoSelect.innerHTML = "<option value=''>Seleccione un terminado</option>";
        
        terminados.forEach(t => {
            if (!esCombinacionDuplicada(productoId, t.id.toString(), fila)) {
                const option = document.createElement("option");
                option.value = t.id;
                
                let precioAMostrar = t.precioPublico || 0;
                if (window.tipoClienteGlobal === "JURIDICO") {
                    precioAMostrar = t.precioPorEncargo || 0;
                }
                
                option.textContent = `Medida: ${t.medidaTerminadoProducto || 'N/A'} cm`;
                option.setAttribute("data-info", `Medida: ${t.medidaTerminadoProducto} cm`);
                option.setAttribute("data-publico", t.precioPublico);
                option.setAttribute("data-mayor", t.precioPorMayor);
                option.setAttribute("data-encargo", t.precioPorEncargo);
                
                if (terminadoSeleccionado && t.id.toString() === terminadoSeleccionado) {
                    option.selected = true;
                }
                
                terminadoSelect.appendChild(option);
            }
        });
        
        if (terminadoSeleccionado) {
            // Actualizar precio automáticamente cuando se selecciona un terminado
            setTimeout(() => {
                actualizarPrecio(fila);
                calcularTotalOrden();
            }, 100);
        }
        
        return true; // Retornar éxito
        
    } catch (error) {
        console.error(`Error cargando terminados para producto ${productoId}:`, error);
        terminadoSelect.innerHTML = "<option value=''>Error al cargar</option>";
        return false; // Retornar error
    }
}

function cargarTerminadosEdicion(productoSelect) {
    const fila = productoSelect.closest('tr');
    const productoId = productoSelect.value;
    
    if (productoId) {
        cargarTerminadosParaFila(fila, productoId);
        actualizarDescripcion(fila);
    } else {
        const terminadoSelect = fila.querySelector("select[name='terminadoId']");
        terminadoSelect.innerHTML = "<option value=''>Seleccione un terminado</option>";
        actualizarPrecio(fila);
    }
}

function aumentar(btn) {
    const input = btn.previousElementSibling;
    input.value = parseInt(input.value) + 1;
    const fila = btn.closest("tr");
    actualizarPrecio(fila);
}

function disminuir(btn) {
    const input = btn.nextElementSibling;
    if (parseInt(input.value) > 1) {
        input.value = parseInt(input.value) - 1;
        const fila = btn.closest("tr");
        actualizarPrecio(fila);
    }
}

function agregarFila() {
    const tabla = document.getElementById("tablaDetalle").querySelector("tbody");
    const nuevaFila = tabla.rows[0].cloneNode(true);
    
    // Limpiar valores de la nueva fila
    nuevaFila.querySelectorAll("input").forEach(input => {
        if (input.name === "cantidad") {
            input.value = 1;
        } else {
            input.value = "";
        }
    });
    
    // Limpiar selects de la nueva fila
    nuevaFila.querySelectorAll("select").forEach(select => {
        if (select.name === "productoId") {
            select.selectedIndex = 0;
            select.title = "Seleccionar producto"; // Asegurar título para accesibilidad
            // Cambiar a función de edición para nuevas filas
            select.onchange = function() { cambiarProductoEdicion(this); };
        } else if (select.name === "terminadoId") {
            select.innerHTML = "<option value=''>Seleccione un terminado</option>";
            select.selectedIndex = 0;
            select.title = "Seleccionar terminado"; // Asegurar título para accesibilidad
            // Agregar evento para actualizar precio
            select.onchange = function() { actualizarPrecio(this.closest('tr')); };
        }
    });
    
    // Cambiar eventos de los botones de cantidad
    const btnAumentar = nuevaFila.querySelector("button[onclick*='aumentar']");
    const btnDisminuir = nuevaFila.querySelector("button[onclick*='disminuir']");
    if (btnAumentar) btnAumentar.onclick = function() { aumentarEdicion(this); };
    if (btnDisminuir) btnDisminuir.onclick = function() { disminuirEdicion(this); };
    
    // Asegurar que el botón de eliminar funcione correctamente
    const botonEliminar = nuevaFila.querySelector("button[onclick*='eliminarFilaEspecifica']");
    if (botonEliminar) {
        botonEliminar.onclick = function() { eliminarFilaEspecifica(this); };
    }
    
    // Remover atributos de datos de edición
    nuevaFila.removeAttribute('data-detalle-id');
    
    tabla.appendChild(nuevaFila);
    // Actualizar disponibilidad después de agregar nueva fila
    setTimeout(() => actualizarDisponibilidadProductos(), 100);
    calcularTotalOrden();
    
    console.log('➕ Nueva fila agregada en modo edición con tipo cliente:', window.tipoClienteGlobal);
}

function eliminarFila() {
    const tabla = document.getElementById("tablaDetalle").querySelector("tbody");
    if (tabla.rows.length > 1) {
        tabla.deleteRow(tabla.rows.length - 1);
        setTimeout(() => actualizarDisponibilidadProductos(), 100);
        calcularTotalOrden();
    }
}

function eliminarFilaEspecifica(botonEliminar) {
    const tabla = document.getElementById("tablaDetalle").querySelector("tbody");

    // No permitir eliminar si solo queda una fila
    if (tabla.rows.length <= 1) {
        Swal.fire({
            title: 'No se puede eliminar',
            text: 'Debe mantener al menos un producto en la orden.',
            icon: 'warning',
            confirmButtonText: 'Aceptar',
            confirmButtonColor: '#d0a736'
        });
        return;
    }

    const fila = botonEliminar.closest("tr");
    const filaIndex = Array.from(tabla.rows).indexOf(fila);

    // Confirmar eliminación con SweetAlert2
    Swal.fire({
        title: '¿Eliminar producto?',
        text: '¿Está seguro de que desea eliminar este producto de la orden?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Sí, eliminar',
        cancelButtonText: 'Cancelar',
        confirmButtonColor: '#d33',
        cancelButtonColor: '#d0a736'
    }).then((result) => {
        if (result.isConfirmed) {
            tabla.deleteRow(filaIndex);

            // Actualizar disponibilidad de productos después de eliminar
            setTimeout(() => actualizarDisponibilidadProductos(), 100);
            calcularTotalOrden();

            // Mostrar confirmación de eliminación
            Swal.fire({
                title: '¡Eliminado!',
                text: 'El producto ha sido eliminado de la orden.',
                icon: 'success',
                confirmButtonText: 'Aceptar',
                confirmButtonColor: '#d0a736',
                timer: 2000
            });
        }
    });
}

function actualizarDescripcion(fila) {
    const productoSelect = fila.querySelector("select[name='productoId']");
    const terminadoSelect = fila.querySelector("select[name='terminadoId']");
    const descripcionInput = fila.querySelector("input[name='descripcion']");

    const productoNombre = productoSelect.options[productoSelect.selectedIndex]?.getAttribute("data-nombre") || "";
    const terminadoInfo = terminadoSelect.options[terminadoSelect.selectedIndex]?.getAttribute("data-info") || "";

    let descripcion = "";
    if (productoNombre) {
        descripcion = productoNombre;
    }
    if (terminadoInfo) {
        descripcion += " - " + terminadoInfo;
    }

    descripcionInput.value = descripcion.trim();
}

document.addEventListener("change", function (e) {
    const fila = e.target.closest("tr");

    if (e.target.name === "productoId") {
        // Solo ejecutar si no hay un handler específico asignado
        if (e.target.onchange && e.target.onchange.toString().includes('cambiarProductoEdicion')) {
            return; // Dejar que el handler específico maneje esto
        }
        const productoId = e.target.value;
        const selectTerminado = fila.querySelector(".select-terminado");

        selectTerminado.innerHTML = "<option value=''>Cargando...</option>";

        fetch(`/terminados/por-producto/${productoId}`)
        .then(res => res.json())
        .then(data => {
            console.log("Terminados recibidos:", data);
            selectTerminado.innerHTML = "<option value=''>Seleccione un terminado</option>";
            data.forEach(t => {
                console.log("Terminado:", t);
                if (!esCombinacionDuplicada(productoId, t.id.toString(), fila)) {
                    const option = document.createElement("option");
                    option.value = t.id;
                    
                    let precioAMostrar = t.precioPublico || 0;
                    if (window.tipoClienteGlobal === "JURIDICO") {
                        precioAMostrar = t.precioPorEncargo || 0;
                    }
                    
                    option.textContent = `Medida: ${t.medidaTerminadoProducto || 'N/A'} cm`;
                    option.setAttribute("data-info", `Medida: ${t.medidaTerminadoProducto} cm`);
                    option.setAttribute("data-publico", t.precioPublico);
                    option.setAttribute("data-mayor", t.precioPorMayor);
                    option.setAttribute("data-encargo", t.precioPorEncargo);
                    selectTerminado.appendChild(option);
                }
            });
            actualizarDescripcion(fila);
        })
        .catch(err => {
            selectTerminado.innerHTML = "<option value=''>Error al cargar</option>";
            console.error("Error cargando terminados:", err);
        });
        
        actualizarDescripcion(fila);
        actualizarPrecio(fila);
    }

    if (e.target.name === "terminadoId") {
        const productoId = fila.querySelector("select[name='productoId']").value;
        const terminadoId = e.target.value;

        if (productoId && terminadoId && esCombinacionDuplicada(productoId, terminadoId, fila)) {
            Swal.fire({
                title: 'Combinación duplicada',
                text: 'Esta combinación de producto y terminado ya está seleccionada en otra fila.',
                icon: 'warning',
                confirmButtonText: 'Aceptar',
                confirmButtonColor: '#d0a736'
            });
            e.target.selectedIndex = 0;
            actualizarPrecio(fila);
            return;
        }

        actualizarDescripcion(fila);
        actualizarPrecio(fila);

        // Actualizar disponibilidad de productos sin recargar completamente
        setTimeout(() => actualizarDisponibilidadProductos(), 100);
    }
});

function actualizarTextoTerminados(fila) {
    const terminadoSelect = fila.querySelector("select[name='terminadoId']");
    
    Array.from(terminadoSelect.options).forEach(option => {
        if (option.value !== "") {
            const publico = option.getAttribute("data-publico");
            const encargo = option.getAttribute("data-encargo");
            const medidaInfo = option.getAttribute("data-info");
            
            let precioAMostrar = publico;
            if (window.tipoClienteGlobal === "JURIDICO") {
                precioAMostrar = encargo;
            }
            
            const medida = medidaInfo ? medidaInfo.replace("Medida: ", "").replace(" cm", "") : "N/A";
            option.textContent = `Medida: ${medida} cm`;
        }
    });
}

function actualizarPrecio(fila) {
    const terminadoSelect = fila.querySelector("select[name='terminadoId']");
    const cantidadInput = fila.querySelector("input[name='cantidad']");
    const valorInput = fila.querySelector("input[name='valor']");

    console.log('🔄 Actualizando precio para fila');
    console.log('Terminado seleccionado:', terminadoSelect.value);

    // Si no hay terminado seleccionado, solo resetear si no estamos en modo edición inicial
    if (!terminadoSelect.value || terminadoSelect.selectedIndex === 0) {
        console.log('❌ No hay terminado seleccionado');
        // En modo edición, preservar el valor original si existe
        const esFilaEdicion = fila.hasAttribute('data-detalle-id');
        if (!esFilaEdicion || !valorInput.value || valorInput.value === '0') {
            valorInput.value = 0;
        }
        calcularTotalOrden();
        return;
    }

    const selectedOption = terminadoSelect.selectedOptions[0];
    const publico = selectedOption?.getAttribute("data-publico");
    const mayor = selectedOption?.getAttribute("data-mayor");
    const encargo = selectedOption?.getAttribute("data-encargo");

    console.log('💰 Precios obtenidos:');
    console.log('Público:', publico);
    console.log('Mayor:', mayor);
    console.log('Encargo:', encargo);
    console.log('Tipo cliente:', window.tipoClienteGlobal);
    
    // Fallback: intentar obtener tipo cliente desde ordenData si no está definido
    if (!window.tipoClienteGlobal) {
        console.log('🔍 Debug - ordenData disponible:', typeof ordenData !== 'undefined');
        console.log('🔍 Debug - window.ordenData disponible:', typeof window.ordenData !== 'undefined');
        
        if (typeof ordenData !== 'undefined' && ordenData.usuario) {
            window.tipoClienteGlobal = ordenData.usuario.tipoUsuario.nombre.toUpperCase();
            console.log('🔄 Fallback - Tipo cliente obtenido de ordenData:', window.tipoClienteGlobal);
        } else if (typeof window.ordenData !== 'undefined' && window.ordenData.usuario) {
            window.tipoClienteGlobal = window.ordenData.usuario.tipoUsuario.nombre.toUpperCase();
            console.log('🔄 Fallback - Tipo cliente obtenido de window.ordenData:', window.tipoClienteGlobal);
        } else {
            console.log('❌ No se pudo obtener tipoClienteGlobal - ordenData no disponible');
        }
    }

    const cantidad = parseInt(cantidadInput.value) || 1;

    let precioFinal = 0;

    if (publico && encargo && mayor) {
        if (window.tipoClienteGlobal === "NATURAL") {
            precioFinal = parseFloat(publico);
        } else if (window.tipoClienteGlobal === "JURIDICO") {
            if (cantidad <= 2) {
                precioFinal = parseFloat(encargo);
            } else {
                precioFinal = parseFloat(mayor);
            }
        } else {
            // Fallback: si no se puede determinar el tipo, usar precio público por defecto
            console.log('⚠️ Tipo cliente no detectado, usando precio público por defecto');
            precioFinal = parseFloat(publico);
        }
    }

    console.log('🎯 Precio final calculado:', precioFinal);

    // Actualizar precio siempre que tengamos un precio válido
    if (precioFinal > 0) {
        valorInput.value = precioFinal;
        console.log('✅ Precio actualizado en el input');
    } else if (!valorInput.value || valorInput.value === '0') {
        // Solo resetear a 0 si el campo está vacío o es 0
        valorInput.value = 0;
        console.log('⚠️ Precio reseteado a 0');
    }
    calcularTotalOrden();
}

function calcularTotalOrden() {
    let total = 0;
    const filas = document.querySelectorAll("#tablaDetalle tbody tr");

    filas.forEach(fila => {
        const cantidad = parseFloat(fila.querySelector("input[name='cantidad']").value) || 0;
        const valor = parseFloat(fila.querySelector("input[name='valor']").value) || 0;
        total += cantidad * valor;
    });

    const totalElement = document.getElementById("totalOrden");
    const inputTotalElement = document.getElementById("inputTotalOrden");
    
    if (totalElement) {
        totalElement.textContent = total.toLocaleString("es-CO");
    }
    if (inputTotalElement) {
        inputTotalElement.value = total;
    }
}

document.addEventListener("input", function (e) {
    if (e.target.name === "cantidad" || e.target.name === "valor") {
        calcularTotalOrden();
    }
});

document.addEventListener("change", function (e) {
    if (e.target.name === "terminadoId") {
        const fila = e.target.closest("tr");
        actualizarPrecio(fila);
    }
});

function cambiarProductoEdicion(productoSelect) {
    const fila = productoSelect.closest('tr');
    const productoId = productoSelect.value;
    const terminadoSelect = fila.querySelector("select[name='terminadoId']");
    const valorInput = fila.querySelector("input[name='valor']");
    
    // Resetear valor inmediatamente al cambiar producto
    valorInput.value = 0;
    
    if (productoId) {
        terminadoSelect.innerHTML = "<option value=''>Cargando terminados...</option>";
        cargarTerminadosParaFila(fila, productoId).then(() => {
            // Después de cargar, actualizar textos y precio
            actualizarTextoTerminados(fila);
            if (terminadoSelect.value) {
                actualizarPrecio(fila);
            }
        });
        actualizarDescripcion(fila);
    } else {
        terminadoSelect.innerHTML = "<option value=''>Seleccione un terminado</option>";
    }
    
    calcularTotalOrden();
}

function actualizarPrecioEdicion(fila) {
    actualizarPrecio(fila);
}

function aumentarEdicion(btn) {
    const input = btn.previousElementSibling;
    input.value = parseInt(input.value) + 1;
    const fila = btn.closest("tr");
    actualizarPrecio(fila);
}

function disminuirEdicion(btn) {
    const input = btn.nextElementSibling;
    if (parseInt(input.value) > 1) {
        input.value = parseInt(input.value) - 1;
        const fila = btn.closest("tr");
        actualizarPrecio(fila);
    }
}

document.addEventListener("input", function (e) {
    if (e.target.name === "cantidad") {
        const fila = e.target.closest("tr");
        actualizarPrecio(fila);
    }
    if (e.target.name === "cantidad" || e.target.name === "valor") {
        calcularTotalOrden();
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const fechaInput = document.querySelector('input[type="datetime-local"][name="fechaEntrega"]');

    if (fechaInput) {
        const hoy = new Date();
        const yyyy = hoy.getFullYear();
        const mm = String(hoy.getMonth() + 1).padStart(2, '0');
        const dd = String(hoy.getDate()).padStart(2, '0');

        const fechaMax = `${yyyy}-${mm}-${dd}`;
        fechaInput.max = fechaMax;
    }
    
    // Cargar terminados para filas en modo edición
    const filasEdicion = document.querySelectorAll("tr[data-detalle-id]");
    filasEdicion.forEach(fila => {
        const terminadoSelect = fila.querySelector("select[name='terminadoId']");
        const productoSelect = fila.querySelector("select[name='productoId']");
        
        if (terminadoSelect && productoSelect) {
            const productoId = terminadoSelect.getAttribute("data-producto");
            const terminadoId = terminadoSelect.getAttribute("data-selected");
            
            if (productoId) {
                cargarTerminadosParaFila(fila, productoId, terminadoId);
            }
        }
    });
    
    // Solo calcular total si no hay filas en modo edición
    if (filasEdicion.length === 0) {
        calcularTotalOrden();
    }
});

// ========== FUNCIONES DE BÚSQUEDA DE USUARIOS ==========
// Las funciones de búsqueda están implementadas directamente en orden-form.html
// para evitar conflictos de carga de scripts