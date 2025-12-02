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
    
    console.log("🔍 Actualizando disponibilidad de productos para", filas.length, "filas");
    
    // Cache para evitar múltiples peticiones del mismo producto
    const terminadosCache = new Map();
    
    for (const fila of filas) {
        const productoSelect = fila.querySelector("select[name='productoId']");
        const terminadoSelect = fila.querySelector("select[name='terminadoId']");
        const productoActual = productoSelect.value;
        const terminadoActual = terminadoSelect.value;
        
        console.log("🔍 Verificando fila con producto actual:", productoActual, "terminado actual:", terminadoActual);
        
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
                    console.log("📥 Cargados", terminados.length, "terminados para producto", productoId);
                } catch (error) {
                    console.error(`Error cargando terminados para producto ${productoId}:`, error);
                    terminadosCache.set(productoId, []);
                }
            }
            
            const terminados = terminadosCache.get(productoId);
            const tieneDisponibles = terminados.some(terminado => 
                !esCombinacionDuplicada(productoId, terminado.id.toString(), fila)
            );
            
            console.log("✅ Producto", productoId, "tiene disponibles:", tieneDisponibles);
            
            // Habilitar/deshabilitar la opción basado en disponibilidad
            option.disabled = !tieneDisponibles;
            
            // Si el producto actual se volvió no disponible, resetear (pero no en modo edición inicial)
            if (productoId === productoActual && !tieneDisponibles && !fila.hasAttribute('data-detalle-id')) {
                productoSelect.value = "";
                terminadoSelect.innerHTML = "<option value=''>Seleccione un terminado</option>";
                actualizarDescripcion(fila);
                actualizarPrecio(fila);
            }
        }
    }
    
    console.log("✅ Actualización de disponibilidad completada");
}

// Función helper para determinar si una orden es de cliente jurídico
function esOrdenDeClienteJuridico() {
    // Verificar si estamos en modo edición buscando filas con data-detalle-id
    const hayFilasEdicion = document.querySelector("tr[data-detalle-id]") !== null;
    
    if (hayFilasEdicion) {
        // En modo edición, intentar usar ordenData primero
        if (window.ordenData && window.ordenData.usuario && window.ordenData.usuario.tipoUsuario) {
            const tipoOrden = window.ordenData.usuario.tipoUsuario.nombre.toUpperCase();
            console.log('🔍 esOrdenDeClienteJuridico() - Usando ordenData:', tipoOrden);
            return tipoOrden === "JURIDICO";
        }
        
        // Fallback 1: usar tipoClienteGlobal si está disponible
        if (window.tipoClienteGlobal) {
            console.log('🔍 esOrdenDeClienteJuridico() - Fallback tipoClienteGlobal:', window.tipoClienteGlobal);
            return window.tipoClienteGlobal === "JURIDICO";
        }
        
        // Fallback 2: verificar en DOM
        const clienteInfo = document.querySelector("#datosCliente");
        if (clienteInfo) {
            const tipoTexto = clienteInfo.textContent || "";
            if (tipoTexto.includes("JURIDICO")) {
                console.log('🔍 esOrdenDeClienteJuridico() - Detectado JURIDICO en DOM');
                return true;
            }
            if (tipoTexto.includes("NATURAL")) {
                console.log('🔍 esOrdenDeClienteJuridico() - Detectado NATURAL en DOM');
                return false;
            }
        }
        
        console.log('⚠️ esOrdenDeClienteJuridico() - No se pudo determinar tipo en modo edición');
        return false; // Por seguridad, permitir recálculos si no podemos determinarlo
    } else {
        // En modo creación, usar el tipoClienteGlobal
        const esJuridico = window.tipoClienteGlobal === "JURIDICO";
        console.log('🔍 esOrdenDeClienteJuridico() - Modo creación:', esJuridico);
        return esJuridico;
    }
}

function actualizarClienteInfo() {
    if (window.usuarioSeleccionadoGlobal) {
        window.tipoClienteGlobal = window.usuarioSeleccionadoGlobal.tipoUsuario.nombre.toUpperCase();
        
        document.querySelectorAll("#tablaDetalle tbody tr").forEach(fila => {
            actualizarTextoTerminados(fila);
            
            // Para filas en modo edición de órdenes de clientes jurídicos, no recalcular precios automáticamente
            const esFilaEdicion = fila.hasAttribute('data-detalle-id');
            const valorInput = fila.querySelector("input[name='valor']");
            const tieneValorExistente = valorInput && valorInput.value && valorInput.value !== '0' && valorInput.value !== '';
            
            if (!(esFilaEdicion && esOrdenDeClienteJuridico() && tieneValorExistente)) {
                // Solo actualizar precio si no es fila de edición de orden de cliente jurídico con valor existente
                actualizarPrecio(fila);
            }
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
            // En modo edición, solo recalcular el total, NO el precio individual
            const esFilaEdicion = fila.hasAttribute('data-detalle-id');
            const valorInput = fila.querySelector("input[name='valor']");
            const tieneValorExistente = valorInput && valorInput.value && valorInput.value !== '0' && valorInput.value !== '';
            
            setTimeout(() => {
                if (esFilaEdicion && esOrdenDeClienteJuridico() && tieneValorExistente) {
                    console.log('🔒 Carga inicial: Preservando precio de BD para orden de cliente jurídico');
                    // Solo recalcular total, mantener precio original de BD
                    calcularTotalOrden();
                } else {
                    // Para nuevas filas o clientes naturales, actualizar precio normalmente
                    actualizarPrecio(fila);
                    calcularTotalOrden();
                }
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
        
        // Solo actualizar precio si no es edición de cliente jurídico con valor existente
        const esFilaEdicion = fila.hasAttribute('data-detalle-id');
        const valorInput = fila.querySelector("input[name='valor']");
        const tieneValorExistente = valorInput && valorInput.value && valorInput.value !== '0' && valorInput.value !== '';
        
        if (!(esFilaEdicion && esOrdenDeClienteJuridico() && tieneValorExistente)) {
            actualizarPrecio(fila);
        } else {
            calcularTotalOrden();
        }
    }
}

function aumentar(btn) {
    const input = btn.previousElementSibling;
    const valorActual = parseInt(input.value);
    if (valorActual < 50) {
        input.value = valorActual + 1;
        const fila = btn.closest("tr");
        actualizarPrecio(fila);
    } else {
        Swal.fire({
            title: 'Límite alcanzado',
            text: 'La cantidad máxima permitida por producto es de 50 unidades.',
            icon: 'warning',
            confirmButtonText: 'Aceptar',
            confirmButtonColor: '#d0a736'
        });
    }
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

    // Verificar si estamos en modo edición ANTES de usar esFilaEdicion
    const esFilaEdicionLocal = fila.hasAttribute('data-detalle-id');
    
    if (publico && encargo && mayor) {
        // Determinar el tipo de cliente correcto para el cálculo
        let tipoClienteParaCalculo = window.tipoClienteGlobal;
        
        // En modo edición o si detectamos contexto de cliente jurídico, usar el tipo apropiado
        if (window.ordenData && window.ordenData.usuario && window.ordenData.usuario.tipoUsuario && window.ordenData.usuario.tipoUsuario.nombre) {
            tipoClienteParaCalculo = window.ordenData.usuario.tipoUsuario.nombre.toUpperCase();
            console.log('💡 Usando tipo de cliente de la orden para cálculo:', tipoClienteParaCalculo);
        } else if (esOrdenDeClienteJuridico()) {
            tipoClienteParaCalculo = "JURIDICO";
            console.log('💡 Detectado contexto de cliente jurídico para cálculo');
        }
        
        console.log('🧮 Calculando precio con tipo:', tipoClienteParaCalculo, 'cantidad:', cantidad);
        
        if (tipoClienteParaCalculo === "NATURAL") {
            precioFinal = parseFloat(publico);
            console.log('💰 Cliente NATURAL - Precio público:', precioFinal);
        } else if (tipoClienteParaCalculo === "JURIDICO") {
            if (cantidad <= 2) {
                precioFinal = parseFloat(encargo);
                console.log('💰 Cliente JURIDICO (<=2) - Precio por encargo:', precioFinal);
            } else {
                precioFinal = parseFloat(mayor);
                console.log('💰 Cliente JURIDICO (>2) - Precio por mayor:', precioFinal);
            }
        } else {
            // Fallback: si no se puede determinar el tipo, usar precio público por defecto
            console.log('⚠️ Tipo cliente no detectado, usando precio público por defecto');
            precioFinal = parseFloat(publico);
        }
    }

    console.log('🎯 Precio final calculado:', precioFinal);

    const tieneValorExistente = valorInput.value && valorInput.value !== '0' && valorInput.value !== '';
    
    console.log('🔍 Debug modo edición:');
    console.log('- Es fila de edición:', esFilaEdicionLocal);
    console.log('- Es orden de cliente jurídico:', esOrdenDeClienteJuridico());
    console.log('- Tiene valor existente:', tieneValorExistente, '(valor actual:', valorInput.value, ')');
    console.log('- tipoClienteGlobal:', window.tipoClienteGlobal);
    
    if (window.ordenData && window.ordenData.usuario) {
        console.log('- Tipo usuario de la orden:', window.ordenData.usuario.tipoUsuario.nombre);
    }

    // Verificar si se está cambiando de terminado/producto
    const terminadoOriginal = fila.getAttribute('data-detalle-id');
    const terminadoActual = terminadoSelect.value;
    const seCambioTerminado = terminadoOriginal && terminadoActual && terminadoOriginal !== terminadoActual;
    
    // Para órdenes de clientes jurídicos en modo edición, preservar precios existentes 
    // SOLO si no se está cambiando el terminado
    if (esFilaEdicionLocal && esOrdenDeClienteJuridico() && tieneValorExistente && !seCambioTerminado) {
        console.log('🔒 Preservando precio original para orden de cliente jurídico en edición (sin cambio de terminado)');
        // No actualizar el precio, mantener el valor original
    } else if (precioFinal > 0) {
        // Actualizar precio normalmente para nuevas filas, cambios de terminado, o cuando no hay valor existente
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
    const valorActual = parseInt(input.value);
    if (valorActual < 50) {
        input.value = valorActual + 1;
        const fila = btn.closest("tr");
        
        // Disparar evento input para que lo maneje el event listener principal
        input.dispatchEvent(new Event('input', { bubbles: true }));
    } else {
        Swal.fire({
            title: 'Límite alcanzado',
            text: 'La cantidad máxima permitida por producto es de 50 unidades.',
            icon: 'warning',
            confirmButtonText: 'Aceptar',
            confirmButtonColor: '#d0a736'
        });
    }
}

function disminuirEdicion(btn) {
    const input = btn.nextElementSibling;
    if (parseInt(input.value) > 1) {
        input.value = parseInt(input.value) - 1;
        const fila = btn.closest("tr");
        
        // Disparar evento input para que lo maneje el event listener principal
        input.dispatchEvent(new Event('input', { bubbles: true }));
    }
}

document.addEventListener("input", function (e) {
    if (e.target.name === "cantidad") {
        const fila = e.target.closest("tr");
        
        // Para órdenes de clientes jurídicos en modo edición, no recalcular precio al cambiar cantidad
        const esFilaEdicion = fila.hasAttribute('data-detalle-id');
        const valorInput = fila.querySelector("input[name='valor']");
        const terminadoSelect = fila.querySelector("select[name='terminadoId']");
        const tieneValorExistente = valorInput && valorInput.value && valorInput.value !== '0' && valorInput.value !== '';
        const hayTerminadoSeleccionado = terminadoSelect && terminadoSelect.value && terminadoSelect.value !== '';
        
        // Solo bloquear el recálculo si es edición de jurídico con valor existente Y hay terminado seleccionado
        const debeBloquearRecalculo = esFilaEdicion && esOrdenDeClienteJuridico() && tieneValorExistente && hayTerminadoSeleccionado;
        
        if (!debeBloquearRecalculo) {
            // Actualizar precio normalmente
            actualizarPrecio(fila);
        } else {
            // Solo recalcular total sin cambiar precio individual
            calcularTotalOrden();
        }
    }
    
    // Siempre recalcular total cuando cambie cantidad o valor
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
    const cargasTerminados = [];
    
    filasEdicion.forEach(fila => {
        const terminadoSelect = fila.querySelector("select[name='terminadoId']");
        const productoSelect = fila.querySelector("select[name='productoId']");
        
        if (terminadoSelect && productoSelect) {
            const productoId = terminadoSelect.getAttribute("data-producto");
            const terminadoId = terminadoSelect.getAttribute("data-selected");
            
            if (productoId) {
                const promesaCarga = cargarTerminadosParaFila(fila, productoId, terminadoId);
                cargasTerminados.push(promesaCarga);
            }
        }
    });
    
    // Después de cargar todos los terminados, actualizar disponibilidad
    if (filasEdicion.length > 0) {
        Promise.all(cargasTerminados).then(() => {
            // Dar tiempo para que se carguen todos los terminados
            setTimeout(() => {
                actualizarDisponibilidadProductos();
                calcularTotalOrden();
            }, 200);
        });
    } else {
        // Solo calcular total si no hay filas en modo edición
        calcularTotalOrden();
    }
});

// ========== FUNCIONES DE BÚSQUEDA DE USUARIOS ==========
// Las funciones de búsqueda están implementadas directamente en orden-form.html
// para evitar conflictos de carga de scripts