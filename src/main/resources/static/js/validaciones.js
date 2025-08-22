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
calcularTotalFactura();
}

function eliminarFila() {
const tabla = document.getElementById("tablaDetalle").querySelector("tbody");
if (tabla.rows.length > 1) {
    tabla.deleteRow(tabla.rows.length - 1);
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
                option.textContent = `${t.medidaTerminadoProducto}`;
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
}

if (e.target.name === "productoId") {
    actualizarDescripcion(fila);
    actualizarPrecio(fila);
}
});

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
    precioFinal = cantidad >= 3 ? mayor : encargo;
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
const fechaInput = document.querySelector('input[type="date"][name="fechaVenta"]');

const hoy = new Date();
const yyyy = hoy.getFullYear();
const mm = String(hoy.getMonth() + 1).padStart(2, '0');
const dd = String(hoy.getDate()).padStart(2, '0');

const fechaMax = `${yyyy}-${mm}-${dd}`;
fechaInput.max = fechaMax;
});