/* ---------- Helpers DOM ---------- */
const qs  = (sel, root = document) => root.querySelector(sel);
const set = (sel, value) => { const el = qs(sel); if (el) el.textContent = value; };

/* ---------- Fechas sin desfasajes por zona horaria ---------- */

// Crea un Date a medianoche LOCAL (no UTC)
function dateFromYMDLocal(iso /* 'YYYY-MM-DD' */) {
    const [y, m, d] = iso.split('-').map(Number);
    return new Date(y, m - 1, d); // local midnight
}

// Devuelve 'YYYY-MM-DD' leyendo componentes LOCALES
function toISO(d) {
    if (!d) return null;
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
}

// Acepta ISO o dd/mm/aaaa y devuelve Date (local)
function parseDateSmart(v) {
    if (!v) return null;
    if (v instanceof Date && !isNaN(v)) return v;

    const s = String(v).trim();

    // ISO estricta: YYYY-MM-DD => crear LOCAL (no new Date(s) que es UTC)
    if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
        return dateFromYMDLocal(s);
    }

    // dd/mm/yyyy o dd-mm-yyyy
    const m = s.match(/^(\d{1,2})[\/-](\d{1,2})[\/-](\d{4})$/);
    if (m) {
        const [_, d, mo, y] = m;
        const iso = `${y}-${mo.padStart(2, '0')}-${d.padStart(2, '0')}`;
        return dateFromYMDLocal(iso);
    }

    // Último intento: que lo resuelva JS. (puede traer hora → igual NO usamos toISOString)
    const d = new Date(s);
    return isNaN(d) ? null : d;
}

// Para mostrar en UI
function formatDmy(iso /* 'YYYY-MM-DD' */) {
    if (!iso) return '-';
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
}

/* ---------- Mapear claves flexibles ---------- */
function normKey(k){return k.toString().normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase().replace(/[^a-z0-9_]/g,'');}

function pickKey(obj, cands){
    const dict={}; Object.keys(obj).forEach(k=>dict[normKey(k)]=k);
    for(const c of cands){const w=normKey(c); if(dict[w]) return dict[w];}
    return null;
}

function inferKeyMap(row){
    const k=(c)=>pickKey(row,c);
    return {
        id:        k(['id','codigo','identificador']),
        titulo:    k(['titulo','título','title','nombre']),
        desc:      k(['descripcion','descripción','description','detalle']),
        categoria: k(['categoria','categoría','category','tipo']),
        fecha:     k(['fecha','fecha del hecho','fecha_del_hecho','fechasuceso','fechaacontecimiento','date']),
        fechaCarga:k(['fechacarga','fecha de carga','fecha_carga']),
        lat:       k(['lat','latitud','latitude']),
        long:      k(['long','lon','lng','longitud','longitude']),
        origen:    k(['origen','fuente']),
        creador:   k(['creador','usuario','autor']),
        roles:     k(['roles','permisos']),
        etiquetas: k(['etiquetas','tags','palabrasclave']),
        media:     k(['media','multimedia'])
    };
}

function normalizeHecho(row, map){
    const fh = map.fecha ? parseDateSmart(row[map.fecha]) : null;
    const fc = map.fechaCarga ? parseDateSmart(row[map.fechaCarga]) : null;

    // media flexible
    let media = row[map.media] || {};
    if (typeof media === 'string') media = { imagenUrl: media };

    // Soportar "Imagen"/"Image" como atajo
    const imagenCruda = row.imagenUrl || row.Imagen || row.Image || row.image;

    // si el id no viene, lo dejamos vacío (se completa en fallback)
    return {
        id:           row[map.id] != null ? String(row[map.id]) : '',
        titulo:       String(row[map.titulo] ?? '').trim(),
        descripcion:  String(row[map.desc]   ?? '').trim(),
        categoria:    String(row[map.categoria] ?? '').trim(),
        fecha:        toISO(fh),
        fechaCarga:   toISO(fc),
        lat:          row[map.lat]  != null ? Number(String(row[map.lat]).replace(',','.'))  : null,
        long:         row[map.long] != null ? Number(String(row[map.long]).replace(',','.')) : null,
        origen:       row[map.origen]  ?? null,
        creador:      row[map.creador] ?? null,
        roles:        Array.isArray(row[map.roles])     ? row[map.roles]     : [],
        etiquetas:    Array.isArray(row[map.etiquetas]) ? row[map.etiquetas] : [],
        media: {
            imagenUrl: (media.imagenUrl || imagenCruda) ?? null,
            videoUrl:   media.videoUrl  || row.videoUrl || null
        }
    };
}

/* ---------- Carga del hecho (API + fallback JSON) ---------- */
async function fetchHechoById(id){
    // API real
    try{
        const r = await fetch(`/api/hechos/${encodeURIComponent(id)}`);
        if(r.ok){
            const h = await r.json();
            // si tu API ya usa estas keys, devolvemos directo:
            if (h && h.id) return h;
        }
    }catch{}

    // Fallback JSON local
    const res = await fetch('../data/desastres_tecnologicos_argentina_20.json');
    const arr = await res.json();
    if(!Array.isArray(arr) || !arr.length) return null;

    const map = inferKeyMap(arr[0]);
    const norm = arr.map(r => normalizeHecho(r,map));

    // Demo: si no tienen ID, creamos uno por índice
    norm.forEach((h,i)=>{ if(!h.id) h.id = String(i+1); });

    return norm.find(h => h.id === String(id)) || null;
}

/* ---------- Render UI ---------- */
function renderMedia(h){
    const wrap = qs('#mediaWrap');
    wrap.innerHTML = '';
    if(h?.media?.videoUrl){
        wrap.innerHTML = `<div class="ratio ratio-16x9"><iframe src="${h.media.videoUrl}" allowfullscreen></iframe></div>`;
        return;
    }
    if(h?.media?.imagenUrl){
        wrap.innerHTML = `<img src="${h.media.imagenUrl}" alt="Imagen del hecho"/>`;
    }
}

function renderRoles(roles){
    const cont = qs('#hRoles');
    cont.innerHTML = '';
    (roles||[]).forEach(r=>{
        const span = document.createElement('span');
        span.className = 'mm-badge-soft mm-badge';
        span.textContent = r;
        cont.appendChild(span);
    });
}

function renderTags(tags){
    const ul = qs('#hTags');
    ul.innerHTML = '';
    (tags||[]).forEach(t=>{
        const li = document.createElement('li');
        li.textContent = t;
        ul.appendChild(li);
    });
}

function initMap(h){
    const mapDiv = qs('#mapDetalle');
    if (!mapDiv) return;

    const Leaf = window.L;
    if (!Leaf) {
        mapDiv.innerHTML = '<div class="text-muted p-4">Mapa no disponible.</div>';
        console.warn('Leaflet no está cargado (window.L es undefined)');
        return;
    }

    // if (typeof L === 'undefined'){
    //     mapDiv.innerHTML = '<div class="text-muted p-4">Mapa no disponible (Leaflet no cargado).</div>';
    //     return;
    // }

    if (h.lat != null && h.long != null) {
        const map = Leaf.map('mapDetalle', {
            center: [h.lat, h.long],
            zoom: 12,
            scrollWheelZoom: true
        });
        Leaf.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap'
        }).addTo(map);
        Leaf.marker([h.lat, h.long]).addTo(map)
            .bindPopup(h.titulo || `Hecho #${h.id}`)
            .openPopup();
    } else {
        mapDiv.innerHTML = '<div class="text-muted p-4">Sin coordenadas para mostrar mapa.</div>';
    }
}

/* ---------- Init ---------- */
(async function init(){
    const id = new URLSearchParams(location.search).get('id');
    //if (!id) { set('#hTitulo','Hecho no encontrado'); return; }

    const h = {}// await fetchHechoById(id);
    //if (!h) { set('#hTitulo','Hecho no encontrado'); return; }

    // Título / bajada
/*    set('#hTitulo',      h.titulo || `Hecho #${h.id}`);
    set('#hBajada',      h.categoria ? `Categoría: ${h.categoria}` : '—');

    // Meta
    set('#hCategoria',   h.categoria || '—');
    set('#hFecha',       formatDmy(h.fecha));
    set('#hFechaCarga',  formatDmy(h.fechaCarga));
    set('#hLat',         (h.lat ?? '—'));
    set('#hLong',        (h.long ?? '—'));
    set('#hOrigen',      h.origen || '—');

    // Texto
    set('#hDescripcion', h.descripcion || 'Sin descripción.');

    // Creador + roles + etiquetas
    if (qs('#hCreador')) {
        const creadorTxt = typeof h.creador === 'string'
            ? h.creador
            : (h.creador?.nombre || '—');
        set('#hCreador', creadorTxt);
    }
    renderRoles(h.roles);
    */
    h.id = Number(document.getElementById('hId').textContent)
    h.lat = Number(document.getElementById('hLat').textContent)
    h.long = Number(document.getElementById('hLong').textContent)
    h.titulo = document.getElementById('hTitulo').textContent

    // Media y mapa
    // renderMedia(h);
    initMap(h);

    // Acciones demo
    const btnReportar = qs('#btnReportar');
    if (btnReportar) btnReportar.addEventListener('click', ()=> alert('Gracias por tu reporte. Nuestro equipo lo revisará.'));
    const btnEliminar = qs('#btnEliminar');
    if (btnEliminar) btnEliminar.addEventListener('click', ()=> alert('Solicitud enviada. Un moderador la evaluará.'));
})();

// Solicitud de eliminacion
(() => { // IIFE (Immediately Invoked Function Expression) o sea, funcion que se define y se ejecuta automaticamente. Sirve para: aislar el scope (lo que declare dentro no queda global), ejecuta el setup una sola vez (corre apenas se carga el archivo y deja listo los listeners, contadores, etc).
  const MIN = 500;
  const txt       = document.getElementById('justificacion');
  const counter   = document.getElementById('just-counter');
  const btnEnviar = document.getElementById('btn-enviar');
  const modalEl   = document.getElementById('modalSolicitud');

  if (!txt || !counter || !btnEnviar || !modalEl) return;   // antes de empezar, si falta cualquiera de esos elementos, termina con la ejecucion.

  // Contador + habilitar/deshabilitar botón
  txt.addEventListener('input', () => {
    const len = txt.value.trim().length;
    counter.textContent = `${len} / ${MIN}`;
    btnEnviar.disabled  = (len < MIN);
    txt.classList.toggle('is-invalid', len < MIN); // "prende/apaga" una clase CSS en un nodo.
  });

  // Enviar solicitud
  btnEnviar.addEventListener('click', async () => {
    const justificacion = txt.value.trim(); // lee el texto y le saca espacios y saltos de linea al inicio y al final asi 500 espacios no cuentan
    if (justificacion.length < MIN) return; // doble verificacion (front y back)

    // obtener id del hecho y del usuario (ajustar)
    const params    = new URLSearchParams(location.search); // location.search es lo que esta despues del '?', es decir, el id. new URLSearchParams(...) lo parsea en un objeto para leer parametros por nombre
    const hechoId   = window.hechoId || Number(params.get('id')); // primero intenta usar window.hechoId (una variable global que podria haber sido definida antes) si eso no existe, cae el plan B: toma el id de la URL y lo convierte a Number.
    const usuarioId = window.currentUserId || 1; // de la misma manera, intenta usar userId global, si no esta, pone 1 como default
    // en produccion el id del hecho y el usuario se toman de la sesion del backend y nos permitiran hacer cosas dependiendo el tipo de usuario, ej: 500 caracteres como minimo. Mostrar/ocultar un boton.

    btnEnviar.disabled = true;

    try {
      const resp = await fetch('/api/solicitudes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idHecho: Number(hechoId),
          idusuario: Number(usuarioId),
          justificacion
        })
      });

      if (!resp.ok) {
        const text = await resp.text();
        showToast(`Error al enviar la solicitud: ${text || resp.status}`, 'bg-danger');
        btnEnviar.disabled = false;
        return;
      }

      showToast('Solicitud enviada exitosamente. Un administrador la revisará.', 'bg-success');

      // Cerrar el modal y resetear
      const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
      modal.hide();
      txt.value = '';
      counter.textContent = `0 / ${MIN}`;
      btnEnviar.disabled = true;
      txt.classList.remove('is-invalid');

    } catch (err) {
      showToast('Error de red. Intentá nuevamente.', 'bg-danger');
      btnEnviar.disabled = false;
    }
  });

  // Toasts básicos con Bootstrap
  function showToast(message, cls = 'bg-dark') {
    // contenedor si no existe
    let area = document.getElementById('toastArea');
    if (!area) {
      area = document.createElement('div');
      area.id = 'toastArea';
      area.className = 'toast-container position-fixed bottom-0 end-0 p-3';
      area.setAttribute('aria-live', 'polite');
      area.setAttribute('aria-atomic', 'true');
      document.body.appendChild(area);
    }

    // toast
    const toast = document.createElement('div');
    toast.className = `toast text-white ${cls}`;
    toast.setAttribute('role', 'status');
    toast.innerHTML = `
      <div class="d-flex">
        <div class="toast-body">${message}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto"
                data-bs-dismiss="toast" aria-label="Cerrar"></button>
      </div>
    `;
    area.appendChild(toast);

    const t = new bootstrap.Toast(toast, { delay: 3500 });
    t.show();
  }
})();
