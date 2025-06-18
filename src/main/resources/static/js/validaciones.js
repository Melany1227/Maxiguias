let tipoClienteGlobal = "";
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
        const option = document.createElement("option");
        option.value = t.id;
        option.textContent = `${t.medidaTerminadoProducto}`;
        option.setAttribute("data-info", `${t.medidaTerminadoProducto}`);
        selectTerminado.appendChild(option);
        option.setAttribute("data-publico", t.precioPublico);
        option.setAttribute("data-mayor", t.precioPorMayor);
        option.setAttribute("data-encargo", t.precioPorEncargo);
        });
        actualizarDescripcion(fila);
    })
    .catch(err => {
        selectTerminado.innerHTML = "<option value=''>Error al cargar</option>";
        console.error("Error cargando terminados:", err);
    });
}

if (e.target.name === "terminadoId" || e.target.name === "productoId") {
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