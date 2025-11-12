let tipoClienteGlobal = "";

// Función para verificar si una combinación ya está seleccionada
function esCombinacionDuplicada(productoId, terminadoId, filaActual) {
    const filas = document.querySelectorAll("#tablaDetalle tbody tr");
    
    for (let fila of filas) {
        if (fila === filaActual) continue; // Saltar la fila actual
        
        const prodSeleccionado = fila.querySelector("select[name='productoId']").value;
        const termSeleccionado = fila.querySelector("select[name='terminadoId']").value;
        
        if (prodSeleccionado === productoId && termSeleccionado === terminadoId) {
            return true;
        }
    }
    
    return false;
}

// Función para verificar si un producto tiene terminados disponibles
async function productoTieneTerminadosDisponibles(productoId, filaActual) {
    try {
        const response = await fetch(`/terminados/por-producto/${productoId}`);
        const terminados = await response.json();
        
        // Verificar si hay al menos un terminado que no esté siendo usado en otra fila
        return terminados.some(terminado => 
            !esCombinacionDuplicada(productoId, terminado.id.toString(), filaActual)
        );
    } catch (error) {
        console.error("Error verificando terminados:", error);
        return true; // En caso de error, mostrar el producto
    }
}

// Función para actualizar las opciones de productos en una fila específica
async function actualizarOpcionesProducto(fila) {
    const productoSelect = fila.querySelector("select[name='productoId']");
    const productoActual = productoSelect.value;
    
    // Obtener todas las opciones originales (guardar referencia)
    if (!window.opcionesProductosOriginales) {
        window.opcionesProductosOriginales = Array.from(productoSelect.options)
            .filter(option => option.value !== "")
            .map(option => ({
                value: option.value,
                text: option.textContent,
                dataNombre: option.getAttribute("data-nombre")
            }));
    }
    
    // Limpiar opciones excepto la primera
    productoSelect.innerHTML = '<option value="">Seleccione un producto</option>';
    
    // Agregar solo productos que tengan terminados disponibles
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
    
    // Restaurar selección si el producto sigue disponible
    if (productoActual) {
        productoSelect.value = productoActual;
        // Si el producto ya no está disponible, limpiar terminados
        if (!productoSelect.value) {
            const selectTerminado = fila.querySelector(".select-terminado");
            selectTerminado.innerHTML = "<option value=''>Seleccione un terminado</option>";
        }
    }
}

// Función para actualizar todas las filas con productos disponibles
async function actualizarTodasLasOpcionesProducto() {
    const filas = document.querySelectorAll("#tablaDetalle tbody tr");
    for (const fila of filas) {
        await actualizarOpcionesProducto(fila);
    }
}

function actualizarDocumento() {
const select = document.getElementById("clienteSelect");
const option = select.options[select.selectedIndex];
const tipoStr = option.getAttribute("data-tipo");
const doc = option.getAttribute("data-doc");

const match = tipoStr.match(/nombre\s*=\s*(\w+)/);
const tipoNombre = match ? match[1].toUpperCase() : "";

const label = document.getElementById("labelDocumento");

if (tipoNombre === 'JURIDICO') {
    label.textContent = "NIT: " + doc;
} else {
    label.textContent = "Documento: " + doc;
}
tipoClienteGlobal = tipoNombre;

document.querySelectorAll("#tablaDetalle tbody tr").forEach(fila => {
    actualizarPrecio(fila);
    actualizarTextoTerminados(fila);
});

calcularTotalFactura();
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
tabla.appendChild(nuevaFila);
// Actualizar opciones de productos en la nueva fila
actualizarOpcionesProducto(nuevaFila);
calcularTotalFactura();
}

function eliminarFila() {
const tabla = document.getElementById("tablaDetalle").querySelector("tbody");
if (tabla.rows.length > 1) {
    tabla.deleteRow(tabla.rows.length - 1);
    // Actualizar opciones de productos en todas las filas después de eliminar
    setTimeout(() => actualizarTodasLasOpcionesProducto(), 100);
    calcularTotalFactura();
}
}

window.onload = () => {
actualizarDocumento();
calcularTotalFactura();
};

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
        selectTerminado.innerHTML = "<option value=''>Seleccione un terminado</option>";
        data.forEach(t => {
            // Solo agregar terminados que no estén ya seleccionados en otras filas
            if (!esCombinacionDuplicada(productoId, t.id.toString(), fila)) {
                const option = document.createElement("option");
                option.value = t.id;
                
                // Determinar qué precio mostrar según el tipo de cliente
                let precioAMostrar = t.precioPublico;
                if (tipoClienteGlobal === "JURIDICO") {
                    precioAMostrar = t.precioPorEncargo; // Por defecto encargo para jurídicos
                }
                
                option.textContent = `${t.medidaTerminadoProducto} - $${precioAMostrar.toLocaleString('es-CO')}`;
                option.setAttribute("data-info", `${t.medidaTerminadoProducto}`);
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
}

if (e.target.name === "terminadoId") {
    // Verificar si la combinación ya existe (validación adicional)
    const productoId = fila.querySelector("select[name='productoId']").value;
    const terminadoId = e.target.value;
    
    if (productoId && terminadoId && esCombinacionDuplicada(productoId, terminadoId, fila)) {
        alert("Esta combinación de producto y terminado ya está seleccionada en otra fila.");
        e.target.selectedIndex = 0;
        return;
    }
    
    actualizarDescripcion(fila);
    actualizarPrecio(fila);
    
    // Actualizar opciones de productos en otras filas después del cambio
    setTimeout(() => actualizarTodasLasOpcionesProducto(), 100);
}

if (e.target.name === "productoId") {
    actualizarDescripcion(fila);
    actualizarPrecio(fila);
}
});

function actualizarTextoTerminados(fila) {
    const terminadoSelect = fila.querySelector("select[name='terminadoId']");
    
    Array.from(terminadoSelect.options).forEach(option => {
        if (option.value !== "") {
            const publico = option.getAttribute("data-publico");
            const encargo = option.getAttribute("data-encargo");
            const medida = option.getAttribute("data-info");
            
            let precioAMostrar = publico;
            if (tipoClienteGlobal === "JURIDICO") {
                precioAMostrar = encargo;
            }
            
            option.textContent = `${medida} - $${parseInt(precioAMostrar).toLocaleString('es-CO')}`;
        }
    });
}

function actualizarPrecio(fila) {
const terminadoSelect = fila.querySelector("select[name='terminadoId']");
const cantidadInput = fila.querySelector("input[name='cantidad']");
const valorInput = fila.querySelector("input[name='valor']");

const publico = terminadoSelect.selectedOptions[0]?.getAttribute("data-publico");
const mayor = terminadoSelect.selectedOptions[0]?.getAttribute("data-mayor");
const encargo = terminadoSelect.selectedOptions[0]?.getAttribute("data-encargo");

const cantidad = parseInt(cantidadInput.value);

let precioFinal = 0;

if (tipoClienteGlobal === "NATURAL") {
    precioFinal = publico;
} else if (tipoClienteGlobal === "JURIDICO") {
    if (cantidad <= 2) {
        precioFinal = encargo;
    } else {
        precioFinal = mayor;
    }
}

valorInput.value = precioFinal || 0;
calcularTotalFactura();
}

function calcularTotalFactura() {
let total = 0;
const filas = document.querySelectorAll("#tablaDetalle tbody tr");

filas.forEach(fila => {
    const cantidad = parseFloat(fila.querySelector("input[name='cantidad']").value) || 0;
    const valor = parseFloat(fila.querySelector("input[name='valor']").value) || 0;
    total += cantidad * valor;
});

document.getElementById("totalFactura").textContent = total.toLocaleString("es-CO");
document.getElementById("inputTotalFactura").value = total;
}

document.addEventListener("input", function (e) {
if (e.target.name === "cantidad" || e.target.name === "valor") {
    calcularTotalFactura();
}
});

document.addEventListener('DOMContentLoaded', function () {
const fechaInput = document.querySelector('input[type="datetime-local"][name="fechaEntrega"]');

const hoy = new Date();
const yyyy = hoy.getFullYear();
const mm = String(hoy.getMonth() + 1).padStart(2, '0');
const dd = String(hoy.getDate()).padStart(2, '0');

const fechaMax = `${yyyy}-${mm}-${dd}`;
fechaInput.max = fechaMax;
});

// Validación de documento en tiempo real
function validarDocumento(input) {
    const valor = input.value;
    const errorDiv = document.getElementById('error-documento') || createErrorDiv('error-documento', input);
    
    // Limpiar mensaje anterior
    errorDiv.textContent = '';
    errorDiv.style.display = 'none';
    
    if (valor === '') {
        return; // No mostrar error si está vacío
    }
    
    // Validar que solo contenga números
    if (!/^\d+$/.test(valor)) {
        mostrarErrorDocumento(errorDiv, 'El documento solo puede contener números. No se permiten puntos, guiones, espacios o letras.');
        return false;
    }
    
    // Validar longitud
    if (valor.length < 6) {
        mostrarErrorDocumento(errorDiv, 'El documento debe tener al menos 6 dígitos.');
        return false;
    }
    
    if (valor.length > 15) {
        mostrarErrorDocumento(errorDiv, 'El documento no puede tener más de 15 dígitos.');
        return false;
    }
    
    return true;
}

// Validación de teléfono en tiempo real
function validarTelefono(input) {
    const valor = input.value;
    const errorDiv = document.getElementById('error-telefono') || createErrorDiv('error-telefono', input);
    
    // Limpiar mensaje anterior
    errorDiv.textContent = '';
    errorDiv.style.display = 'none';
    
    if (valor === '') {
        return; // No mostrar error si está vacío
    }
    
    // Validar que solo contenga números
    if (!/^\d+$/.test(valor)) {
        mostrarErrorTelefono(errorDiv, 'El teléfono solo puede contener números. No se permiten espacios, guiones o caracteres especiales.');
        return false;
    }
    
    // Validar longitud mínima (10 dígitos)
    if (valor.length < 10) {
        mostrarErrorTelefono(errorDiv, 'El teléfono debe tener al menos 10 dígitos.');
        return false;
    }
    
    // Validar longitud máxima
    if (valor.length > 15) {
        mostrarErrorTelefono(errorDiv, 'El teléfono no puede tener más de 15 dígitos.');
        return false;
    }
    
    return true;
}

// Validación de contraseña en tiempo real
function validarContrasena(input) {
    const valor = input.value;
    const errorDiv = document.getElementById('error-contrasena') || createErrorDiv('error-contrasena', input);
    
    // Limpiar mensaje anterior
    errorDiv.textContent = '';
    errorDiv.style.display = 'none';
    
    if (valor === '') {
        return; // No mostrar error si está vacío
    }
    
    // Lista de validaciones
    const validaciones = [];
    
    // Verificar longitud mínima (8 caracteres)
    if (valor.length < 8) {
        validaciones.push('al menos 8 caracteres');
    }
    
    // Verificar al menos una letra mayúscula
    if (!/[A-Z]/.test(valor)) {
        validaciones.push('al menos una letra mayúscula');
    }
    
    // Verificar al menos una letra minúscula
    if (!/[a-z]/.test(valor)) {
        validaciones.push('al menos una letra minúscula');
    }
    
    // Verificar al menos un número
    if (!/[0-9]/.test(valor)) {
        validaciones.push('al menos un número');
    }
    
    // Verificar al menos un carácter especial
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(valor)) {
        validaciones.push('al menos un carácter especial (!@#$%^&*)');
    }
    
    if (validaciones.length > 0) {
        let mensaje = 'La contraseña no cumple con las políticas de seguridad. Debe tener: ' + validaciones.join(', ') + '.';
        mostrarErrorContrasena(errorDiv, mensaje);
        return false;
    }
    
    return true;
}

function createErrorDiv(id, inputElement) {
    const errorDiv = document.createElement('div');
    errorDiv.id = id;
    errorDiv.className = 'alert alert-danger';
    errorDiv.style.display = 'none';
    errorDiv.style.position = 'absolute';
    errorDiv.style.top = '100%';
    errorDiv.style.left = '0';
    errorDiv.style.right = '0';
    errorDiv.style.zIndex = '1000';
    
    // Asegurar que el padre tenga position relative
    const parent = inputElement.parentNode;
    if (getComputedStyle(parent).position === 'static') {
        parent.style.position = 'relative';
    }
    
    parent.appendChild(errorDiv);
    return errorDiv;
}

function mostrarErrorDocumento(errorDiv, mensaje) {
    errorDiv.textContent = mensaje;
    errorDiv.style.display = 'block';
}

function mostrarErrorTelefono(errorDiv, mensaje) {
    errorDiv.textContent = mensaje;
    errorDiv.style.display = 'block';
}

function mostrarErrorContrasena(errorDiv, mensaje) {
    errorDiv.textContent = mensaje;
    errorDiv.style.display = 'block';
}

// Función para limpiar caracteres no numéricos del input
function limpiarCaracteresInvalidos(input) {
    const valor = input.value;
    const soloNumeros = valor.replace(/[^0-9]/g, '');
    
    if (valor !== soloNumeros) {
        input.value = soloNumeros;
        const errorDiv = document.getElementById('error-documento') || 
            createErrorDiv('error-documento', input);
        mostrarErrorDocumento(errorDiv, 'Se han eliminado caracteres no válidos. Solo se permiten números.');
        
        // Ocultar el error después de 2 segundos
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 2000);
    }
}

// Función para limpiar caracteres no numéricos del teléfono
function limpiarTelefonoInvalido(input) {
    const valor = input.value;
    const soloNumeros = valor.replace(/[^0-9]/g, '');
    
    if (valor !== soloNumeros) {
        input.value = soloNumeros;
        const errorDiv = document.getElementById('error-telefono') || 
            createErrorDiv('error-telefono', input);
        mostrarErrorTelefono(errorDiv, 'Se han eliminado caracteres no válidos. Solo se permiten números.');
        
        // Ocultar el error después de 2 segundos
        setTimeout(() => {
            errorDiv.style.display = 'none';
        }, 2000);
    }
}

// Agregar listener cuando el DOM esté cargado
document.addEventListener('DOMContentLoaded', function() {
    const documentoInputs = document.querySelectorAll('input[name="documento"]');
    
    documentoInputs.forEach(function(documentoInput) {
        // Configurar el input para que solo acepte números
        documentoInput.setAttribute('inputmode', 'numeric');
        documentoInput.setAttribute('pattern', '[0-9]*');
        
        // Validar y limpiar en tiempo real mientras escribe
        documentoInput.addEventListener('input', function() {
            limpiarCaracteresInvalidos(this);
            validarDocumento(this);
        });
        
        // Validar cuando pierde el foco
        documentoInput.addEventListener('blur', function() {
            limpiarCaracteresInvalidos(this);
            validarDocumento(this);
        });
        
        // Prevenir pegado de contenido no numérico
        documentoInput.addEventListener('paste', function(e) {
            e.preventDefault();
            const clipboardData = e.clipboardData || window.clipboardData;
            const pastedData = clipboardData.getData('text');
            const soloNumeros = pastedData.replace(/[^0-9]/g, '');
            
            if (soloNumeros !== pastedData && pastedData !== '') {
                const errorDiv = document.getElementById('error-documento') || 
                    createErrorDiv('error-documento', this);
                mostrarErrorDocumento(errorDiv, 'Solo se pueden pegar números. Se han filtrado los caracteres inválidos.');
                
                setTimeout(() => {
                    errorDiv.style.display = 'none';
                }, 3000);
            }
            
            this.value = soloNumeros;
            validarDocumento(this);
        });
        
        // Prevenir caracteres no numéricos al escribir
        documentoInput.addEventListener('keypress', function(e) {
            // Permitir teclas especiales (backspace, tab, delete, arrows)
            if (e.keyCode === 8 || e.keyCode === 9 || e.keyCode === 46 || 
                (e.keyCode >= 37 && e.keyCode <= 40)) {
                return;
            }
            
            // Bloquear caracteres que no sean números
            if (e.keyCode < 48 || e.keyCode > 57) {
                e.preventDefault();
                const errorDiv = document.getElementById('error-documento') || 
                    createErrorDiv('error-documento', this);
                mostrarErrorDocumento(errorDiv, 'Solo se permiten números en el documento.');
                
                setTimeout(() => {
                    errorDiv.style.display = 'none';
                }, 2000);
            }
        });
        
        // Prevenir arrastrar y soltar contenido no válido
        documentoInput.addEventListener('drop', function(e) {
            e.preventDefault();
            const data = e.dataTransfer.getData('text');
            const soloNumeros = data.replace(/[^0-9]/g, '');
            
            if (soloNumeros !== data && data !== '') {
                const errorDiv = document.getElementById('error-documento') || 
                    createErrorDiv('error-documento', this);
                mostrarErrorDocumento(errorDiv, 'Solo se pueden arrastrar números. Se han filtrado los caracteres inválidos.');
                
                setTimeout(() => {
                    errorDiv.style.display = 'none';
                }, 3000);
            }
            
            this.value = soloNumeros;
            validarDocumento(this);
        });
    });
    
    // Validación de teléfono - aplicar a todos los campos de teléfono
    const telefonoInputs = document.querySelectorAll('input[name="telefono"]');
    
    telefonoInputs.forEach(function(telefonoInput) {
        // Configurar el input para que solo acepte números
        telefonoInput.setAttribute('inputmode', 'numeric');
        telefonoInput.setAttribute('pattern', '[0-9]*');
        
        // Validar y limpiar en tiempo real mientras escribe
        telefonoInput.addEventListener('input', function() {
            limpiarTelefonoInvalido(this);
            validarTelefono(this);
        });
        
        // Validar cuando pierde el foco
        telefonoInput.addEventListener('blur', function() {
            limpiarTelefonoInvalido(this);
            validarTelefono(this);
        });
        
        // Prevenir pegado de contenido no numérico
        telefonoInput.addEventListener('paste', function(e) {
            e.preventDefault();
            const clipboardData = e.clipboardData || window.clipboardData;
            const pastedData = clipboardData.getData('text');
            const soloNumeros = pastedData.replace(/[^0-9]/g, '');
            
            if (soloNumeros !== pastedData && pastedData !== '') {
                const errorDiv = document.getElementById('error-telefono') || 
                    createErrorDiv('error-telefono', this);
                mostrarErrorTelefono(errorDiv, 'Solo se pueden pegar números. Se han filtrado los caracteres inválidos.');
                
                setTimeout(() => {
                    errorDiv.style.display = 'none';
                }, 3000);
            }
            
            this.value = soloNumeros;
            validarTelefono(this);
        });
        
        // Prevenir caracteres no numéricos al escribir
        telefonoInput.addEventListener('keypress', function(e) {
            // Permitir teclas especiales (backspace, tab, delete, arrows)
            if (e.keyCode === 8 || e.keyCode === 9 || e.keyCode === 46 || 
                (e.keyCode >= 37 && e.keyCode <= 40)) {
                return;
            }
            
            // Bloquear caracteres que no sean números
            if (e.keyCode < 48 || e.keyCode > 57) {
                e.preventDefault();
                const errorDiv = document.getElementById('error-telefono') || 
                    createErrorDiv('error-telefono', this);
                mostrarErrorTelefono(errorDiv, 'Solo se permiten números en el teléfono.');
                
                setTimeout(() => {
                    errorDiv.style.display = 'none';
                }, 2000);
            }
        });
        
        // Prevenir arrastrar y soltar contenido no válido
        telefonoInput.addEventListener('drop', function(e) {
            e.preventDefault();
            const data = e.dataTransfer.getData('text');
            const soloNumeros = data.replace(/[^0-9]/g, '');
            
            if (soloNumeros !== data && data !== '') {
                const errorDiv = document.getElementById('error-telefono') || 
                    createErrorDiv('error-telefono', this);
                mostrarErrorTelefono(errorDiv, 'Solo se pueden arrastrar números. Se han filtrado los caracteres inválidos.');
                
                setTimeout(() => {
                    errorDiv.style.display = 'none';
                }, 3000);
            }
            
            this.value = soloNumeros;
            validarTelefono(this);
        });
    });
    
    // Validación de contraseña - aplicar a todos los campos de contraseña
    const contrasenaInputs = document.querySelectorAll('input[name="contrasena"], input[type="password"]');
    
    contrasenaInputs.forEach(function(contrasenaInput) {
        // Validar en tiempo real mientras escribe
        contrasenaInput.addEventListener('input', function() {
            validarContrasena(this);
        });
        
        // Validar cuando pierde el foco
        contrasenaInput.addEventListener('blur', function() {
            validarContrasena(this);
        });
    });
    
    // Validación de confirmación de contraseña
    const confirmarContrasenaInputs = document.querySelectorAll('input[name="confirmarContrasena"]');
    
    confirmarContrasenaInputs.forEach(function(confirmarInput) {
        confirmarInput.addEventListener('input', function() {
            validarConfirmacionContrasena(this);
        });
        
        confirmarInput.addEventListener('blur', function() {
            validarConfirmacionContrasena(this);
        });
    });
});

// Función para validar que las contraseñas coincidan
function validarConfirmacionContrasena(input) {
    const valor = input.value;
    const contrasenaInput = document.querySelector('input[name="contrasena"]');
    const errorDiv = document.getElementById('error-confirmar-contrasena') || createErrorDiv('error-confirmar-contrasena', input);
    
    // Limpiar mensaje anterior
    errorDiv.textContent = '';
    errorDiv.style.display = 'none';
    
    if (valor === '') {
        return; // No mostrar error si está vacío
    }
    
    if (contrasenaInput && valor !== contrasenaInput.value) {
        mostrarErrorContrasena(errorDiv, 'Las contraseñas no coinciden.');
        return false;
    }
    
    return true;
}