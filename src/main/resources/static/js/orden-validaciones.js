let tipoClienteGlobal = "";

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

function actualizarClienteInfo() {
    if (usuarioSeleccionadoGlobal) {
        tipoClienteGlobal = usuarioSeleccionadoGlobal.tipoUsuario.nombre.toUpperCase();
        
        document.querySelectorAll("#tablaDetalle tbody tr").forEach(fila => {
            actualizarPrecio(fila);
            actualizarTextoTerminados(fila);
        });
        
        calcularTotalOrden();
    }
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
                if (tipoClienteGlobal === "JURIDICO") {
                    precioAMostrar = t.precioPorEncargo || 0;
                }
                
                option.textContent = `Medida: ${t.medidaTerminadoProducto || 'N/A'} - $${precioAMostrar.toLocaleString('es-CO')}`;
                option.setAttribute("data-info", `Medida: ${t.medidaTerminadoProducto}`);
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
            // Solo calcular total después de cargar, sin modificar el precio existente
            setTimeout(() => calcularTotalOrden(), 100);
        }
        
    } catch (error) {
        console.error(`Error cargando terminados para producto ${productoId}:`, error);
        terminadoSelect.innerHTML = "<option value=''>Error al cargar</option>";
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
    nuevaFila.querySelectorAll("input").forEach(input => input.value = input.name === "cantidad" ? 1 : "");
    // Limpiar selects de la nueva fila
    nuevaFila.querySelectorAll("select").forEach(select => {
        if (select.name === "productoId") {
            select.selectedIndex = 0;
        } else if (select.name === "terminadoId") {
            select.innerHTML = "<option value=''>Seleccione un terminado</option>";
            select.selectedIndex = 0;
        }
    });
    
    // Asegurar que el botón de eliminar funcione correctamente
    const botonEliminar = nuevaFila.querySelector("button[onclick*='eliminarFilaEspecifica']");
    if (botonEliminar) {
        botonEliminar.onclick = function() { eliminarFilaEspecifica(this); };
    }
    
    tabla.appendChild(nuevaFila);
    actualizarOpcionesProducto(nuevaFila);
    calcularTotalOrden();
}

function eliminarFila() {
    const tabla = document.getElementById("tablaDetalle").querySelector("tbody");
    if (tabla.rows.length > 1) {
        tabla.deleteRow(tabla.rows.length - 1);
        setTimeout(() => actualizarTodasLasOpcionesProducto(), 100);
        calcularTotalOrden();
    }
}

function eliminarFilaEspecifica(botonEliminar) {
    const tabla = document.getElementById("tablaDetalle").querySelector("tbody");
    
    // No permitir eliminar si solo queda una fila
    if (tabla.rows.length <= 1) {
        alert("Debe mantener al menos un producto en la orden.");
        return;
    }
    
    const fila = botonEliminar.closest("tr");
    const filaIndex = Array.from(tabla.rows).indexOf(fila);
    
    // Confirmar eliminación
    if (confirm("¿Está seguro de que desea eliminar este producto de la orden?")) {
        tabla.deleteRow(filaIndex);
        
        // Actualizar opciones de productos después de eliminar
        setTimeout(() => actualizarTodasLasOpcionesProducto(), 100);
        calcularTotalOrden();
    }
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
                    if (tipoClienteGlobal === "JURIDICO") {
                        precioAMostrar = t.precioPorEncargo || 0;
                    }
                    
                    option.textContent = `Medida: ${t.medidaTerminadoProducto || 'N/A'} - $${precioAMostrar.toLocaleString('es-CO')}`;
                    option.setAttribute("data-info", `Medida: ${t.medidaTerminadoProducto}`);
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
            alert("Esta combinación de producto y terminado ya está seleccionada en otra fila.");
            e.target.selectedIndex = 0;
            actualizarPrecio(fila);
            return;
        }
        
        actualizarDescripcion(fila);
        actualizarPrecio(fila);
        
        setTimeout(() => actualizarTodasLasOpcionesProducto(), 100);
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
            if (tipoClienteGlobal === "JURIDICO") {
                precioAMostrar = encargo;
            }
            
            const medida = medidaInfo ? medidaInfo.replace("Medida: ", "") : "N/A";
            option.textContent = `Medida: ${medida} - $${parseInt(precioAMostrar).toLocaleString('es-CO')}`;
        }
    });
}

function actualizarPrecio(fila) {
    const terminadoSelect = fila.querySelector("select[name='terminadoId']");
    const cantidadInput = fila.querySelector("input[name='cantidad']");
    const valorInput = fila.querySelector("input[name='valor']");

    // Si no hay terminado seleccionado, solo resetear si no estamos en modo edición inicial
    if (!terminadoSelect.value || terminadoSelect.selectedIndex === 0) {
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

    const cantidad = parseInt(cantidadInput.value) || 1;

    let precioFinal = 0;

    if (publico && encargo && mayor) {
        if (tipoClienteGlobal === "NATURAL") {
            precioFinal = parseFloat(publico);
        } else if (tipoClienteGlobal === "JURIDICO") {
            if (cantidad <= 2) {
                precioFinal = parseFloat(encargo);
            } else {
                precioFinal = parseFloat(mayor);
            }
        }
    }

    // Solo actualizar si tenemos un precio válido o si no es modo edición inicial
    if (precioFinal > 0) {
        valorInput.value = precioFinal;
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

function cambiarProductoEdicion(productoSelect) {
    const fila = productoSelect.closest('tr');
    const productoId = productoSelect.value;
    const terminadoSelect = fila.querySelector("select[name='terminadoId']");
    const valorInput = fila.querySelector("input[name='valor']");
    
    // Resetear valor inmediatamente al cambiar producto
    valorInput.value = 0;
    
    if (productoId) {
        terminadoSelect.innerHTML = "<option value=''>Cargando terminados...</option>";
        cargarTerminadosParaFila(fila, productoId);
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