// ====== CONFIG ======
/* Para trabajar con el JSON local de prueba => DATA_URL apuntando al .json.
 * Para usar el backend, cambiar a: const DATA_URL = '/api/hechos';
 */
const DATA_URL = '../data/desastres_tecnologicos_argentina_20.json';

// Punto de vista por defecto (Argentina)
const FALLBACK_CENTER = [-38.4, -63.6];
const FALLBACK_ZOOM = 5;

// ====== UTILIDADES ======
const $ = (sel) => document.querySelector(sel);
const exists = (el) => !!(el && el instanceof HTMLElement);

// Normaliza claves para buscarlas sin acentos/espacios
function normKey(k) {
    return k
        .toString()
        .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .replace(/[^a-z0-9_]/g, '');
}

// Detecta la key a usar según candidatos (p.ej. ['lat','latitude','latitud'])
function pickKey(obj, candidates) {
    const dict = {};
    for (const k of Object.keys(obj)) dict[normKey(k)] = k;
    for (const c of candidates) {
        const want = normKey(c);
        if (dict[want]) return dict[want];
    }
    return null;
}

// Helpers para normalizar texto y keywords
const normalizeStr = s => (s || '')
    .toString()
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();

let KEYWORDS = []; // chips activos

function matchesKeywords(h, arr) {
    if (!arr.length) return true;
    const haystack = normalizeStr(`${h.titulo} ${h.categoria} ${h.descripcion || ''}`);
    return arr.some(k => haystack.includes(normalizeStr(k)));
}

// Parse de fecha flexible: ISO, dd/mm/aaaa, dd-mm-aaaa, etc.
function parseDateSmart(v) {
    if (!v) return null;
    if (v instanceof Date) return v;

    const s = String(v).trim();

    // ISO yyyy-mm-dd
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
        const [y, m, d] = s.split('-').map(Number);
        const dt = new Date(y, m - 1, d);
        return isNaN(dt) ? null : dt;
    }

    // dd/mm/yyyy o dd-mm-yyyy
    const m = s.match(/^(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})$/);
    if (m) {
        const [_, d, mo, y] = m;
        const dt = new Date(Number(y), Number(mo) - 1, Number(d));
        return isNaN(dt) ? null : dt;
    }

    const dt = new Date(s);
    return isNaN(dt) ? null : dt;
}

// Retorna 'YYYY-MM-DD' usando campos locales (sin shift de TZ)
function toISOStrLocal(date) {
    if (!(date instanceof Date) || isNaN(date)) return null;
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

// Normaliza cualquier valor de fecha a 'YYYY-MM-DD'
function toISOShortFromAny(v) {
    if (!v && v !== 0) return null;
    const s = String(v).trim();
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s; // ya es ISO corto
    const dt = parseDateSmart(s);
    return toISOStrLocal(dt);
}

// Convierte cualquier número a float (o null)
function toNum(v) {
    if (v === undefined || v === null || v === '') return null;
    const n = Number(String(v).replace(',', '.'));
    return Number.isFinite(n) ? n : null;
}


function inferKeyMap(row) {
    const key = (cands) => pickKey(row, cands);
    return {
        id: key(['id', 'codigo', 'identificador']),
        titulo: key(['titulo', 'título', 'title', 'nombre']),
        categoria: key(['categoria', 'categoría', 'category', 'tipo']),
        fecha: key(['fecha', 'fecha acontecimiento', 'fechaacontecimiento', 'fecha del hecho', 'fechasuceso', 'date']),
        creado: key(['fecha carga', 'fechacarga', 'creacion', 'creación', 'created_at', 'createdat', 'create_date']),
        descripcion: key(['descripcion', 'descripción', 'description', 'detalle']),
        lat: key(['lat', 'latitud', 'latitude']),
        long: key(['long', 'lon', 'lng', 'longitud', 'longitude'])
    };
}

function normalizeHecho(row, inferredMap = null) {
    const map = inferredMap || inferKeyMap(row);
    const id = row[map.id] ?? null;
    const titulo = row[map.titulo] ?? '';
    const cat = row[map.categoria] ?? '';
    const fecha = row[map.fecha] ?? null;
    const creado = row[map.creado] ?? null;
    const desc = row[map.descripcion] ?? '';
    const lat = row[map.lat];
    const lon = row[map.long];

    return {
        id: String(id ?? ''),
        titulo: String(titulo || '').trim(),
        categoria: String(cat || '').trim(),
        descripcion: String(desc || '').trim(),
        fecha: toISOShortFromAny(fecha),   // acontecimiento
        creado: toISOShortFromAny(creado),  // creación / carga
        lat: toNum(lat),
        long: toNum(lon)
    };
}


// Normaliza una lista heterogénea
function normalizeList(list) {
    if (!Array.isArray(list)) return [];
    const map = list.length ? inferKeyMap(list[0]) : null;
    return list
        .map(r => normalizeHecho(r, map))
        .filter(h => h.lat != null && h.long != null);
}


// Pobla dinámicamente el <select id="fCategoria">
function populateCategoryFilter(hechos) {
    const sel = $('#fCategoria');
    if (!sel) return;

    const first = sel.querySelector('option[value=""]');
    sel.innerHTML = '';
    if (first) sel.appendChild(first);

    const cats = Array.from(new Set(hechos.map(h => h.categoria).filter(Boolean)))
        .sort((a, b) => a.localeCompare(b, 'es', {sensitivity: 'base'}));

    for (const c of cats) {
        const opt = document.createElement('option');
        opt.value = c;
        opt.textContent = c;
        sel.appendChild(opt);
    }
}

// ====== DATOS ======
let HECHOS = [];

async function loadData() {
    try {
        const res = await fetch(DATA_URL, {credentials: 'same-origin'});
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        const arr = Array.isArray(data) ? data : (Array.isArray(data?.data) ? data.data : []);
        HECHOS = normalizeList(arr);
    } catch (e) {
        console.error('No se pudieron cargar los hechos:', e);
        HECHOS = [];
    }
}

// ====== MAPA ======
let map, markersLayer;

function initMap() {
    map = L.map('map', {
        center: FALLBACK_CENTER, zoom: FALLBACK_ZOOM, scrollWheelZoom: true
    });
    window.map = map; // para invalidateSize desde otros scripts

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap'
    }).addTo(map);

    markersLayer = L.layerGroup().addTo(map);
}


function popupHtml(h) {
    const fecha = h.fecha || h.creado || '-';
    // Usamos un botón linkeado al modal con data-* para pasar info del hecho
    return `
    <div class="mm-popup">
      <strong>${h.titulo || '(sin título)'}</strong><br/>
      <small>Categoría:</small> ${h.categoria || '-'}<br/>
      <small>Fecha:</small> ${fecha}
      <div class="mm-popup__actions">
        <a class="mm-link" href="${h.url ? h.url : "hecho-completo.html?id=" + encodeURIComponent(h.id)}">
          <i class="fa-regular fa-eye"></i> Ver más
        </a>
        <button type="button"
                class="mm-link mm-link--danger js-open-solicitud"
                data-id="${String(h.id || '')}"
                data-titulo="${(h.titulo || '').replace(/"/g, '&quot;')}">
          <i class="fa-regular fa-flag"></i> Solicitar eliminación
        </button>
      </div>
    </div>`;
}


function render(list) {
    markersLayer.clearLayers();
    const latlngs = [];

    list.forEach(h => {
        if (h.lat == null || h.long == null) return;
        latlngs.push([h.lat, h.long]);
        L.marker([h.lat, h.long], {title: `${h.id} - ${h.titulo}`})
            .bindPopup(popupHtml(h))
            .addTo(markersLayer);
    });

    const info = $('#resultInfo');
    if (info) info.textContent = `${list.length} resultado${list.length !== 1 ? 's' : ''}`;

    latlngs.length ? map.fitBounds(latlngs, {padding: [20, 20]}) : map.setView(FALLBACK_CENTER, FALLBACK_ZOOM);
}


// ====== FILTROS ======
const inRangeISO = (iso, from, to) => {
    if (!iso) return false;
    if (from && iso < from) return false;
    if (to && iso > to) return false;
    return true;
};

// Obtiene filtros y ajusta “hasta” al fin del día (inclusivo)
function getFilters() {
    const cat = document.getElementById('fCategoria')?.value || '';

    const d1Str = document.getElementById('fAcontDesde')?.value || '';
    const d2Str = document.getElementById('fAcontHasta')?.value || '';
    const cd1Str = document.getElementById('fCreacionDesde')?.value || '';
    const cd2Str = document.getElementById('fCreacionHasta')?.value || '';

    const d1 = d1Str ? parseDateSmart(d1Str) : null;
    const d2 = d2Str ? parseDateSmart(d2Str) : null;
    const cd1 = cd1Str ? parseDateSmart(cd1Str) : null;
    const cd2 = cd2Str ? parseDateSmart(cd2Str) : null;

    if (d1) d1.setHours(0, 0, 0, 0);
    if (d2) d2.setHours(23, 59, 59, 999);
    if (cd1) cd1.setHours(0, 0, 0, 0);
    if (cd2) cd2.setHours(23, 59, 59, 999);

    return {cat, d1, d2, cd1, cd2, keywords: KEYWORDS.slice()};
}

function applyFilters() {
    const {cat, d1, d2, cd1, cd2, keywords} = getFilters();

    const list = HECHOS.filter(h => {
        if (cat && h.categoria !== cat) return false;

        // Acontecimiento
        if (d1 || d2) {
            const fh = h.fecha ? new Date(h.fecha) : null;
            if (!fh) return false;
            if (d1 && fh < d1) return false;
            if (d2 && fh > d2) return false;
        }

        // Creación / carga (si el origen tiene ese dato)
        if (cd1 || cd2) {
            const fc = h.creado ? new Date(h.creado) : null;
            if (!fc) return false;
            if (cd1 && fc < cd1) return false;
            if (cd2 && fc > cd2) return false;
        }

        // Keywords
        if (!matchesKeywords(h, keywords)) return false;

        return true;
    });

    render(list);
}


function clearFilters() {
    ['#fCategoria', '#fAcontDesde', '#fAcontHasta', '#fCreacionDesde', '#fCreacionHasta', '#fTexto']
        .forEach(sel => {
            const el = $(sel);
            if (exists(el)) el.value = '';
        });

    // limpiar chips
    KEYWORDS = [];
    renderKeywords();

    render(HECHOS);
    setTimeout(() => map.invalidateSize(), 200);
}

// ====== INIT ======
document.addEventListener('DOMContentLoaded', async () => {
    initMap();
    //await loadData();
    const hechos = getHechosReales()
    populateCategoryFilter(hechos);
    render(hechos);
    $('#modoCurado')?.addEventListener('click', () => simularRadioButton('#modoIrrestricto', '#modoCurado'))
    $('#modoIrrestricto')?.addEventListener('click', () => simularRadioButton('#modoCurado', '#modoIrrestricto'))
    // Botones
    //$('#btnAplicar')?.addEventListener('click', applyFilters);
    //$('#btnLimpiar')?.addEventListener('click', clearFilters);

    // Enter aplica
    /*['#fCategoria','#fAcontDesde','#fAcontHasta','#fCreacionDesde','#fCreacionHasta','#fTexto']
        .forEach(sel => $(sel)?.addEventListener('keydown', e => {
            if (e.key === 'Enter') { e.preventDefault(); applyFilters(); }
        }));*/

    // Recalcular mapa al abrir/cerrar “Más filtros”
    document.querySelector('.mm-adv')?.addEventListener('toggle', () => {
        setTimeout(() => map.invalidateSize(), 220);
    });
});

function simularRadioButton(modoContrario, modoActual) {
    $(modoContrario).checked = false
    $(modoActual).checked = true
}

function getHechosReales() {
    const containerHechos = document.getElementById('conjuntoDeHechosMapa')
    const divsHechos = containerHechos.querySelectorAll('div.datosHecho')
    return [...divsHechos].map(divHecho => {
        const hecho = {}
        hecho.id = toNum(divHecho.querySelector('span.idHecho').textContent)
        hecho.titulo = String(divHecho.querySelector('span.tituloHecho').textContent).trim()
        hecho.categoria = String(divHecho.querySelector('span.categoriaHecho').textContent).trim()
        hecho.fecha = toISOShortFromAny(new Date(divHecho.querySelector('span.fechaHecho').textContent))
        hecho.lat = toNum(divHecho.querySelector('span.latitudHecho').textContent)
        hecho.long = toNum(divHecho.querySelector('span.longitudHecho').textContent)
        hecho.url = divHecho.querySelector('span.urlHecho').textContent
        return hecho
    })
}

// === Keywords chips UI ===
const kwInput = document.getElementById('fTexto');
const kwBox = document.getElementById('kwChips');

function renderKeywords() {
    if (!kwBox) return;
    kwBox.innerHTML = '';
    KEYWORDS.forEach((k, i) => {
        const el = document.createElement('span');
        el.className = 'chip';
        el.innerHTML = `
        <span class="chip__label">${k}</span>
        <button type="button" class="chip__remove" aria-label="Quitar">×</button>`;
        el.querySelector('.chip__remove').addEventListener('click', () => {
            KEYWORDS.splice(i, 1);
            renderKeywords();
        });
        kwBox.appendChild(el);
    });
}

function addKeyword(raw) {
    const k = (raw || '').trim();
    if (!k) return;
    const nk = normalizeStr(k);
    if (!KEYWORDS.some(x => normalizeStr(x) === nk)) {
        KEYWORDS.push(k);
        renderKeywords();
    }
    if (kwInput) kwInput.value = '';
}

if (kwInput) {
    kwInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ',') {
            e.preventDefault();
            addKeyword(kwInput.value.replace(/,+$/, ''));
        }
    });
    kwInput.addEventListener('blur', () => addKeyword(kwInput.value));
}

renderKeywords();

if (document.getElementById('btnAplicar')) {
    document.getElementById('btnAplicar').addEventListener('click', () => {
        if (kwInput && kwInput.value.trim()) addKeyword(kwInput.value);
        applyFilters();
    });
}

// ===== Modal "Solicitud de eliminación" =====
document.addEventListener("click", (e) => {
    const btn = e.target.closest(".js-open-solicitud");
    if (!btn) return;

    window._hechoIdSeleccionado = Number(btn.dataset.id);

    const titulo = btn.dataset.titulo || "";
    const label = document.getElementById("modalSolicitudLabel");
    if (label && titulo) label.textContent = `Solicitud de eliminación – ${titulo}`;

    const modalEl = document.getElementById("modalSolicitud");
    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    modal.show();
});

//Lógica interna del modal
(() => {
    const MIN = 500;
    const txt = document.getElementById("justificacion");
    const counter = document.getElementById("just-counter");
    const btnEnviar = document.getElementById("btn-enviar");
    const modalEl = document.getElementById("modalSolicitud");

    if (!txt || !counter || !btnEnviar || !modalEl) return;

    txt.addEventListener("input", () => {
        const len = txt.value.trim().length;
        counter.textContent = `${len} / ${MIN}`;
        btnEnviar.disabled = len < MIN;
        txt.classList.toggle("is-invalid", len < MIN);
    });

    btnEnviar.addEventListener("click", async () => {
        const hechoId = window._hechoIdSeleccionado;
        const justificacion = txt.value.trim();
        if (!hechoId || justificacion.length < MIN) return;

        btnEnviar.disabled = true;

        const url = `/hechos/${hechoId}/solicitud-eliminacion`;
        const body = new URLSearchParams({idHecho: String(hechoId), justificacion});

        try {
            const resp = await fetch(url, {method: "POST", body});
            if (!resp.ok) {
                const text = await resp.text();
                showToast(`Error al enviar la solicitud: ${text || resp.status}`, "bg-danger");
                btnEnviar.disabled = false;
                return;
            }

            showToast("Solicitud enviada. Un administrador la revisará.", "bg-success");

            const modal = bootstrap.Modal.getInstance(modalEl);
            modal.hide();
            txt.value = "";
            counter.textContent = `0 / ${MIN}`;
            btnEnviar.disabled = true;
            txt.classList.remove("is-invalid");
        } catch (err) {
            showToast("Error de red. Intentá nuevamente.", "bg-danger");
            btnEnviar.disabled = false;
        }
    });

    function showToast(message, cls = "bg-dark") {
        let area = document.getElementById("toastArea");
        if (!area) {
            area = document.createElement("div");
            area.id = "toastArea";
            area.className = "toast-container position-fixed bottom-0 end-0 p-3";
            document.body.appendChild(area);
        }

        const toast = document.createElement("div");
        toast.className = `toast text-white ${cls}`;
        toast.innerHTML = `
      <div class="d-flex">
        <div class="toast-body">${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto"
                data-bs-dismiss="toast" aria-label="Cerrar"></button>
      </div>`;
        area.appendChild(toast);

        const t = new bootstrap.Toast(toast, {delay: 3500});
        t.show();
    }
})();