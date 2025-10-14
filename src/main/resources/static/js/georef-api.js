// API Georef Argentina para autocompletado geográfico
// https://datos.gob.ar/dataset/georef-ar-api

const GEOREF_BASE_URL = 'https://apis.datos.gob.ar/georef/api';

// Función para buscar municipios por nombre
async function buscarMunicipios(query) {
    if (!query || query.length < 2) return [];
    try {
        const response = await fetch(`${GEOREF_BASE_URL}/municipios?nombre=${encodeURIComponent(query)}&max=10`);
        if (!response.ok) throw new Error('Error en la API');
        const data = await response.json();
        return data.municipios || [];
    } catch (error) {
        console.error('Error buscando municipios:', error);
        return [];
    }
}

// Función para buscar provincias por nombre
async function buscarProvincias(query) {
    if (!query || query.length < 2) return [];
    try {
        const response = await fetch(`${GEOREF_BASE_URL}/provincias?nombre=${encodeURIComponent(query)}&max=10`);
        if (!response.ok) throw new Error('Error en la API');
        const data = await response.json();
        return data.provincias || [];
    } catch (error) {
        console.error('Error buscando provincias:', error);
        return [];
    }
}

// Función para buscar departamentos por nombre
async function buscarDepartamentos(query) {
    if (!query || query.length < 2) return [];
    try {
        const response = await fetch(`${GEOREF_BASE_URL}/departamentos?nombre=${encodeURIComponent(query)}&max=10`);
        if (!response.ok) throw new Error('Error en la API');
        const data = await response.json();
        return data.departamentos || [];
    } catch (error) {
        console.error('Error buscando departamentos:', error);
        return [];
    }
}

// Función genérica para crear datalist con opciones
function crearDatalist(id, opciones, formatter) {
    let datalist = document.getElementById(id);
    if (!datalist) {
        datalist = document.createElement('datalist');
        datalist.id = id;
        document.body.appendChild(datalist);
    }
    datalist.innerHTML = '';
    opciones.forEach(opcion => {
        const option = document.createElement('option');
        option.value = formatter ? formatter(opcion) : opcion.nombre;
        datalist.appendChild(option);
    });
}

// Función para configurar autocompletado en un input
function configurarAutocompletado(inputId, tipo) {
    const input = document.getElementById(inputId);
    if (!input) return;

    const datalistId = `${inputId}-datalist`;
    input.setAttribute('list', datalistId);

    let timeout;
    input.addEventListener('input', function() {
        clearTimeout(timeout);
        timeout = setTimeout(async () => {
            const query = this.value.trim();
            if (query.length < 2) return;

            let opciones = [];
            switch (tipo) {
                case 'provincia':
                    opciones = await buscarProvincias(query);
                    crearDatalist(datalistId, opciones, p => p.nombre);
                    break;
                case 'municipio':
                    opciones = await buscarMunicipios(query);
                    crearDatalist(datalistId, opciones, m => `${m.nombre}, ${m.provincia.nombre}`);
                    break;
                case 'departamento':
                    opciones = await buscarDepartamentos(query);
                    crearDatalist(datalistId, opciones, d => `${d.nombre}, ${d.provincia.nombre}`);
                    break;
            }
        }, 300); // Debounce de 300ms
    });
}

// Exportar funciones para uso en otros módulos
window.GeorefAPI = {
    buscarMunicipios,
    buscarProvincias,
    buscarDepartamentos,
    configurarAutocompletado
};
