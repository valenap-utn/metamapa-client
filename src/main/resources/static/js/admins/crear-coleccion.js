//JS para FUENTES y URLs
(function () {
    const tipoEl = document.getElementById('tipoFuente');
    const urlEl = document.getElementById('url');
    const addBtn = document.getElementById('addFuente');
    const cont = document.getElementById('fuentesContainer');
    const hidden = document.getElementById('fuentesHidden');
    if (!tipoEl || !urlEl || !addBtn || !cont || !hidden) return;

    const fuentes = []; // {tipo, url}

    // Normaliza: agrega https:// si falta y recorta espacios
    function normalizeUrl(v) {
        let s = (v || '').trim();
        if (!s) return '';
        if (!/^https?:\/\//i.test(s)) s = 'https://' + s;
        try {
            s = new URL(s).href;
        } catch (e) { /* deja tal cual si no parsea */
        }
        return s;
    }

    const urlOk = v => /^https?:\/\/\S+\.?\S*/.test(v); // simple pero útil

    function render() {
        cont.innerHTML = '';
        fuentes.forEach((f, i) => {
            const chip = document.createElement('span');
            chip.className = 'chip';
            chip.innerHTML = `
                        <input type="text" style="display: none;" name="fuentes[${i}].tipoOrigen" value="${f.tipo}" />
                        <input type="text" style="display: none;" name="fuentes[${i}].url" value="${f.url}"/>
                        <strong>${f.tipo}</strong> · ${f.url}
                        <button type="button" class="chip__remove" aria-label="Quitar fuente"><span aria-hidden="true">×</span></button>`;
            chip.querySelector('.chip__remove').addEventListener('click', () => {
                fuentes.splice(i, 1);
                render();
            });
            cont.appendChild(chip);
        });
        //hidden.value = JSON.stringify(fuentes);
    }

    function validate() {
        const candidate = normalizeUrl(urlEl.value);
        const ok = urlOk(candidate);
        addBtn.disabled = !ok;
        urlEl.classList.toggle('is-invalid', urlEl.value.trim() !== '' && !ok);
        return ok;
    }

    // Autocorrección al salir del campo / pegar
    function autocorrect() {
        const normalized = normalizeUrl(urlEl.value);
        if (urlEl.value.trim() && urlEl.value !== normalized) {
            urlEl.value = normalized;
        }
        validate();
    }

    addBtn.addEventListener('click', () => {
        const tipo = (tipoEl.value || '').trim() || '—';
        const url = normalizeUrl(urlEl.value);

        if (!urlOk(url)) {
            urlEl.classList.add('is-invalid');
            urlEl.focus();
            return;
        }
        // evita duplicados por URL
        if (fuentes.some(f => f.url.toLowerCase() === url.toLowerCase())) {
            urlEl.value = '';
            validate();
            return;
        }
        fuentes.push({tipo, url});
        tipoEl.value = '';
        urlEl.value = '';
        validate();
        render();
    });

    urlEl.addEventListener('blur', autocorrect);
    urlEl.addEventListener('paste', setTimeout.bind(null, autocorrect, 0));
    urlEl.addEventListener('input', validate);
    urlEl.addEventListener('change', validate);
    urlEl.addEventListener('keydown', e => {
        if (e.key === 'Enter') {
            e.preventDefault();
            addBtn.click();
        }
    });

    validate();
    render();
})();

//JS para CRITERIOS de PERTENENCIA
(function () {
    const tipoSel = document.getElementById('critTipo');
    const wrap = document.getElementById('critValorWrap');
    const addBtn = document.getElementById('addCriterio');
    const chips = document.getElementById('criteriosChips');
    const hidden = document.getElementById('criteriosHidden');
    if (!tipoSel || !wrap || !addBtn || !chips || !hidden) return;

    const criterios = []; // { tipo, label, ...valores }
    agregarCriteriosYaHechos(criterios)
    const todayISO = () => new Date().toISOString().slice(0, 10);

    // --- configuraciones por tipo ---
    const CFG = {
        CATEGORIA: {
            label: 'Categoría',
            render() {
                wrap.innerHTML = `
          <label class="mm-label" for="critValorCategoria">Valor</label>
          <select id="critValorCategoria" class="mm-input" >
            <option value="" disabled selected>Seleccioná una categoría…</option>
            <option value="EVENTO_SANITARIO">Evento sanitario</option>
            <option value="CONTAMINACION">Contaminación</option>
            <option value="DERRAME">Derrame</option>
          </select>
        `;
            },
            read() {
                const el = document.getElementById('critValorCategoria');
                return el && el.value ? {value: el.value, text: el.options[el.selectedIndex].text} : null;
            }
        },

        TITULO: {
            label: 'Título contiene',
            render() {
                wrap.innerHTML = `
          <label class="mm-label" for="critValorText">Valor</label>
          <input id="critValorText" class="mm-input" placeholder="Ej: 'río', 'incendio'…" />
          <small class="mm-help">Mínimo 3 caracteres.</small>
        `;
            },
            read() {
                const el = document.getElementById('critValorText');
                const v = (el?.value || '').trim();
                if (v.length < 3) {
                    el?.classList.add('is-invalid');
                    return null;
                }
                el.classList.remove('is-invalid');
                return {value: v};
            }
        },

        UBICACION: {
            label: 'Ubicación',
            render() {
                wrap.innerHTML = `
          <label class="mm-label" for="critValorUbic">Valor</label>
          <input id="critValorUbic" class="mm-input" placeholder="Provincia / Ciudad / zona…"/>
        `;
            },
            read() {
                const el = document.getElementById('critValorUbic');
                const v = (el?.value || '').trim();
                if (!v) {
                    el?.classList.add('is-invalid');
                    return null;
                }
                el.classList.remove('is-invalid');
                return {value: v};
            }
        },

        // === FECHAS segun lo que hablamos ===
        FECHAACONTECIMIENTO: multiDateCfg('Fecha de acontecimiento', false),
        FECHACARGA: multiDateCfg('Fecha de carga', true)
    };

    // fecha exacta (un solo input)
    function multiDateCfg(label, esConCargaActual) {
        return {
            label,
            render() {
                wrap.innerHTML = `
          <label class="mm-label" for="critFechaInicial">${label}</label>
          <input id="critFechaInicial" type="datetime-local" class="mm-input" >
          <label class="mm-label" for="critFechaFinal">${label}</label>
          <input id="critFechaFinal" type="datetime-local" class="mm-input" value="${esConCargaActual ? todayISO(): ''}" >
        `;
            },
            read() {
                const el = document.getElementById('critFechaInicial');
                const v = el?.value || '';
                if (!v) {
                    el?.classList.add('is-invalid');
                } else {
                    el.classList.remove('is-invalid');
                }
                const el2 = document.getElementById('critFechaFinal');
                const v2 = el2?.value || '';
                if (!v2) {
                    el?.classList.add('is-invalid');
                } else {
                    el2.classList.remove('is-invalid');
                }
                return {desde: v, hasta: v2};
            }
        };
    }

    // fecha de carga = hoy (solo informativo en UI)
    function cargaTodayCfg(label) {
        return {
            label,
            render() {
                const hoy = todayISO();
                wrap.innerHTML = `
          <label class="mm-label">${label}</label>
          <input type="date" class="mm-input" value="${hoy}" disabled>
          <small class="mm-help">Se toma automáticamente la fecha actual (el backend debería confirmarla).</small>
        `;
            },
            read() {
                return {fecha: todayISO()};
            }
        };
    }

    // --- hooks UI ---
    tipoSel.addEventListener('change', () => {
        const cfg = CFG[tipoSel.value];
        if (cfg) cfg.render();
    });

    addBtn.addEventListener('click', () => {
        const tipo = tipoSel.value;
        const cfg = CFG[tipo];
        if (!cfg) return;

        const val = cfg.read();
        if (!val) return;

        const label = buildLabel(cfg.label, val);
        criterios.push({tipo, label, ...val});
        renderChips();
        persist();
    });

    function buildLabel(base, val) {
        if ('text' in val) return `${base}: ${val.text}`;
        if ('value' in val) return `${base}: ${val.value}`;
        if ('fecha' in val) return `${base}: ${val.fecha}`;
        // fallback por si en el futuro hay rango
        if ('desde' in val || 'hasta' in val) {
            const a = val.desde || '—';
            const b = val.hasta || '—';
            return `${base}: ${a} → ${b}`;
        }
        return base;
    }


    function renderChips() {
        const cont = document.getElementById('criteriosChips');
        cont.innerHTML = '';
        criterios.forEach((c, i) => {
            const chip = document.createElement('span');
            chip.className = 'mm-chip';
            chip.innerHTML = `
            <input type="text" style="display: none;" name="criterios[${i}].tipo" value="${c.tipo}"/>
            ${getInputSegunTipo(c, i)}       
            <b>${c.label}</b>
        <button type="button" class="chip__remove" aria-label="Quitar criterio">
          <span aria-hidden="true">×</span>
        </button>`;
            chip.querySelector('.chip__remove').addEventListener('click', () => {
                criterios.splice(i, 1);
                renderChips();
                persist();
            });
            cont.appendChild(chip);
        });
    }

    function getInputSegunTipo(c, i) {
        if(c.tipo === "CATEGORIA" ) {
            return `<input type="text" style="display: none;" name="criterios[${i}].categoria" value="${c.text}"/>`
        } else if( c.tipo === "TITULO") {
            return `<input type="text" style="display: none;" name="criterios[${i}].titulo" value="${c.value}"/>`
        } else if( c.tipo === "FECHAACONTECIMIENTO") {
            return`<input type="datetime-local" style="display: none;" name="criterios[${i}].fechaAcontecimientoInicial" value="${c.desde}"/>` + `<input type="datetime-local" style="display: none;" name="criterios[${i}].fechaAcontecimientoFinal" value="${c.hasta}"/>`
        } else if(c.tipo === "FECHACARGA") {
            return `<input type="datetime-local" style="display: none;" name="criterios[${i}].fechaCargaInicial" value="${c.desde}"/>` +
                `<input type="datetime-local" style="display: none;" name="criterios[${i}].fechaCargaFinal" value="${c.hasta}"/>`
        }
        return ""
    }

    function persist() {
        //hidden.value = JSON.stringify(criterios);
    }

    // estado inicial sugerido
    CFG.TITULO.render();
    function agregarCriteriosYaHechos(criterios) {
        const container = document.getElementById("criteriosChips")
        const criteriosExistentes = container.querySelectorAll("span.mm-chip")
        if(criteriosExistentes.length > 0) {
            criteriosExistentes.forEach((criterio, indice) => {
                const inputTipo = criterio.getElementById("Criterio."+ indice).value

                const cfg = CFG[inputTipo];
                if (!cfg) return;

                const val = cfg.read();
                if (!val) return;

                const label = buildLabel(cfg.label, val);

                if (inputTipo === "TITULO") {
                    const titulo = criterio.getElementsByName(`criterios[${indice}].titulo`).item(0).value
                    criterios.push({tipo: inputTipo,label, value: titulo })
                } else if (inputTipo === "CATEGORIA") {
                    const categoria = criterio.getElementsByName(`criterios[${indice}].categoria`).item(0).value
                    criterios.push({tipo: inputTipo,label, text: categoria })
                } else if (inputTipo === "FECHACARGA") {
                    const fechaInicial = criterio.getElementsByName(`criterios[${indice}].fechaAcontecimientoInicial`).item(0).value
                    const fechaFinal = criterio.getElementsByName(`criterios[${indice}].fechaAcontecimientoFinal`).item(0).value
                    criterios.push({tipo: inputTipo,label, desde: fechaInicial, hasta: fechaFinal })
                } else if (inputTipo === "FECHAACONTECIMIENTO") {
                    const fechaInicial = criterio.getElementsByName(`criterios[${indice}].fechaCargaInicial`).item(0).value
                    const fechaFinal = criterio.getElementsByName(`criterios[${indice}].fechaCargaFinal`).item(0).value
                    criterios.push({tipo: inputTipo, label, desde: fechaInicial, hasta: fechaFinal})
                }
            })
        }
    }
})();