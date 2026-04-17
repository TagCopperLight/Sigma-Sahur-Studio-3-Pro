/**
 * seances.js — Gestion de l'onglet Séances
 */

let editingSeanceId = null;
let calendarView = false;
let calendarInstance = null;

async function loadSeances() {
    try {
        const statut = document.getElementById('filtreStatut').value;
        const params = statut ? `?statut=${statut}` : '';
        const data = await apiGet(`/seances${params}`);

        if (calendarView) {
            if (calendarInstance) {
                calendarInstance.setOption('events', data.map(seanceToEvent));
            }
        } else {
            const tbody = document.getElementById('seancesTbody');
            if (!data.length) {
                tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">Aucune séance</td></tr>';
                return;
            }
            tbody.innerHTML = data.map(s => `
                <tr>
                    <td>${formatDate(s.dateSeance)}</td>
                    <td>${s.heureDebut} – ${s.heureFin}</td>
                    <td>${escapeHtml(s.lieu)}</td>
                    <td>${formatType(s.typeSeance)}</td>
                    <td>${escapeHtml(s.photographeNom)}</td>
                    <td>${escapeHtml(s.clientNom)}</td>
                    <td>${formatStatut(s.statut)}</td>
                    <td>${buildSeanceActions(s)}</td>
                </tr>`).join('');
        }
    } catch (e) {
        showAlert('danger', e.message);
    }
}

function setVueListe() {
    calendarView = false;
    document.getElementById('seancesListeView').style.display = '';
    document.getElementById('seancesCalendrierView').style.display = 'none';
    document.getElementById('btnVueListe').classList.remove('btn-outline-dark');
    document.getElementById('btnVueListe').classList.add('btn-dark', 'active');
    document.getElementById('btnVueCalendrier').classList.remove('btn-dark', 'active');
    document.getElementById('btnVueCalendrier').classList.add('btn-outline-dark');
    loadSeances();
}

function setVueCalendrier() {
    calendarView = true;
    document.getElementById('seancesListeView').style.display = 'none';
    document.getElementById('seancesCalendrierView').style.display = '';
    document.getElementById('btnVueCalendrier').classList.remove('btn-outline-dark');
    document.getElementById('btnVueCalendrier').classList.add('btn-dark', 'active');
    document.getElementById('btnVueListe').classList.remove('btn-dark', 'active');
    document.getElementById('btnVueListe').classList.add('btn-outline-dark');
    initCalendar();
    loadSeances();
}

function initCalendar() {
    if (calendarInstance) return;
    const el = document.getElementById('seancesCalendar');
    calendarInstance = new FullCalendar.Calendar(el, {
        locale: 'fr',
        initialView: 'dayGridMonth',
        headerToolbar: {
            left:   'prev,next today',
            center: 'title',
            right:  'dayGridMonth,timeGridWeek,timeGridDay'
        },
        height: 'auto',
        events: [],
        eventClick: function(info) {
            openSeanceDetail(info.event.extendedProps.seance);
        },
        eventDidMount: function(info) {
            const s = info.event.extendedProps.seance;
            info.el.setAttribute('title', `${s.lieu} — ${formatType(s.typeSeance)}`);
            new bootstrap.Tooltip(info.el, { trigger: 'hover' });
        }
    });
    calendarInstance.render();
}

function seanceToEvent(s) {
    const COULEURS = {
        PLANIFIEE: '#ffc107',
        CONFIRMEE: '#0d6efd',
        TERMINEE:  '#198754',
        ANNULEE:   '#6c757d'
    };
    return {
        id:              String(s.id),
        title:           `${s.heureDebut} ${escapeHtml(s.clientNom)} — ${escapeHtml(s.photographeNom)}`,
        start:           `${s.dateSeance}T${s.heureDebut}`,
        end:             `${s.dateSeance}T${s.heureFin}`,
        backgroundColor: COULEURS[s.statut] || '#6c757d',
        borderColor:     COULEURS[s.statut] || '#6c757d',
        textColor:       s.statut === 'PLANIFIEE' ? '#212529' : '#ffffff',
        extendedProps:   { seance: s }
    };
}

function openSeanceDetail(s) {
    const modifiable = s.statut === 'PLANIFIEE' || s.statut === 'CONFIRMEE';

    document.getElementById('seanceDetailTitre').textContent = `Séance — ${formatDate(s.dateSeance)}`;

    document.getElementById('seanceDetailBody').innerHTML = `
        <dl class="row mb-0">
            <dt class="col-sm-4">Date</dt>
            <dd class="col-sm-8">${formatDate(s.dateSeance)}</dd>
            <dt class="col-sm-4">Horaire</dt>
            <dd class="col-sm-8">${escapeHtml(s.heureDebut)} – ${escapeHtml(s.heureFin)} (${s.dureeMinutes} min)</dd>
            <dt class="col-sm-4">Lieu</dt>
            <dd class="col-sm-8">${escapeHtml(s.lieu)}</dd>
            <dt class="col-sm-4">Type</dt>
            <dd class="col-sm-8">${formatType(s.typeSeance)}</dd>
            <dt class="col-sm-4">Photographe</dt>
            <dd class="col-sm-8">${escapeHtml(s.photographeNom)}</dd>
            <dt class="col-sm-4">Client</dt>
            <dd class="col-sm-8">${escapeHtml(s.clientNom)}</dd>
            <dt class="col-sm-4">Statut</dt>
            <dd class="col-sm-8">${formatStatut(s.statut)}</dd>
            <dt class="col-sm-4">Prix</dt>
            <dd class="col-sm-8">${s.prix.toFixed(2)} €</dd>
        </dl>`;

    let footerHtml = `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fermer</button>`;
    if (modifiable) {
        footerHtml += `<button type="button" class="btn btn-outline-primary"
            onclick="bootstrap.Modal.getInstance(document.getElementById('seanceDetailModal')).hide(); openEditSeance(${s.id})">
            Éditer</button>`;
    }
    footerHtml += `<button type="button" class="btn btn-outline-secondary"
        onclick="bootstrap.Modal.getInstance(document.getElementById('seanceDetailModal')).hide(); openChangerStatut(${s.id}, '${s.statut}')">
        Changer statut</button>`;
    if (s.statut === 'TERMINEE') {
        footerHtml += `<button type="button" class="btn btn-outline-success"
            onclick="telechargerFacture(${s.id})">
            Télécharger facture</button>`;
    }
    document.getElementById('seanceDetailFooter').innerHTML = footerHtml;

    new bootstrap.Modal(document.getElementById('seanceDetailModal')).show();
}

function buildSeanceActions(s) {
    const modifiable = s.statut === 'PLANIFIEE' || s.statut === 'CONFIRMEE';
    let html = '';
    if (modifiable) {
        html += `<button class="btn btn-sm btn-outline-primary me-1" onclick="openEditSeance(${s.id})">Éditer</button>`;
    }
    html += `<button class="btn btn-sm btn-outline-secondary me-1" onclick="openChangerStatut(${s.id}, '${s.statut}')">Statut</button>`;
    if (s.statut === 'TERMINEE') {
        html += `<button class="btn btn-sm btn-outline-success me-1" onclick="telechargerFacture(${s.id})">Facture</button>`;
    }
    if (modifiable) {
        html += `<button class="btn btn-sm btn-outline-danger" onclick="deleteSeance(${s.id})">Supprimer</button>`;
    }
    return html;
}

async function loadPhotographesActifs() {
    const select = document.getElementById('seancePhotographe');
    select.innerHTML = '<option value="">Chargement...</option>';
    const data = await apiGet('/photographes');
    const actifs = data.filter(p => p.statut === 'ACTIF');
    select.innerHTML = actifs.map(p => `<option value="${p.id}">${escapeHtml(p.nom)}</option>`).join('');
}

async function loadClientsSelect() {
    const select = document.getElementById('seanceClient');
    select.innerHTML = '<option value="">Chargement...</option>';
    const data = await apiGet('/clients');
    select.innerHTML = data.map(c => `<option value="${c.id}">${escapeHtml(c.nomComplet)}</option>`).join('');
}

async function openCreateSeance() {
    editingSeanceId = null;
    document.getElementById('seanceModalTitle').textContent = 'Nouvelle séance';
    document.getElementById('seanceForm').reset();
    await Promise.all([loadPhotographesActifs(), loadClientsSelect()]);
    new bootstrap.Modal(document.getElementById('seanceModal')).show();
}

async function openEditSeance(id) {
    try {
        const s = await apiGet(`/seances/${id}`);
        editingSeanceId = id;
        document.getElementById('seanceModalTitle').textContent = 'Modifier la séance';
        await Promise.all([loadPhotographesActifs(), loadClientsSelect()]);
        document.getElementById('seanceDate').value = s.dateSeance;
        document.getElementById('seanceHeure').value = s.heureDebut;
        document.getElementById('seanceDuree').value = s.dureeMinutes;
        document.getElementById('seanceLieu').value = s.lieu;
        document.getElementById('seanceType').value = s.typeSeance;
        document.getElementById('seancePrix').value = s.prix;
        document.getElementById('seancePhotographe').value = s.photographeId;
        document.getElementById('seanceClient').value = s.clientId;
        new bootstrap.Modal(document.getElementById('seanceModal')).show();
    } catch (e) {
        showAlert('danger', e.message);
    }
}

async function saveSeance() {
    const body = {
        dateSeance:    document.getElementById('seanceDate').value,
        heureDebut:    document.getElementById('seanceHeure').value,
        dureeMinutes:  parseInt(document.getElementById('seanceDuree').value),
        lieu:          document.getElementById('seanceLieu').value.trim(),
        typeSeance:    document.getElementById('seanceType').value,
        prix:          parseFloat(document.getElementById('seancePrix').value),
        photographeId: parseInt(document.getElementById('seancePhotographe').value),
        clientId:      parseInt(document.getElementById('seanceClient').value)
    };

    if (!body.dateSeance || !body.heureDebut || !body.lieu || !body.typeSeance ||
        !body.photographeId || !body.clientId) {
        showAlert('warning', 'Veuillez remplir tous les champs obligatoires.');
        return;
    }

    try {
        if (editingSeanceId) {
            await apiPut(`/seances/${editingSeanceId}`, body);
            showAlert('success', 'Séance modifiée avec succès');
        } else {
            await apiPost('/seances', body);
            showAlert('success', 'Séance créée avec succès');
        }
        bootstrap.Modal.getInstance(document.getElementById('seanceModal')).hide();
        loadSeances();
    } catch (e) {
        showAlert('danger', e.message);
    }
}

function openChangerStatut(id, statutActuel) {
    document.getElementById('statutSeanceId').value = id;
    const select = document.getElementById('nouveauStatut');
    select.innerHTML = '';
    const transitions = {
        PLANIFIEE: ['CONFIRMEE', 'ANNULEE'],
        CONFIRMEE: ['TERMINEE', 'ANNULEE'],
        TERMINEE:  [],
        ANNULEE:   []
    };
    const opts = transitions[statutActuel] || [];
    if (!opts.length) {
        showAlert('info', 'Ce statut ne peut plus être modifié.');
        return;
    }
    opts.forEach(s => {
        const o = document.createElement('option');
        o.value = s;
        o.textContent = s;
        select.appendChild(o);
    });
    new bootstrap.Modal(document.getElementById('statutModal')).show();
}

async function saveStatut() {
    const id = document.getElementById('statutSeanceId').value;
    const statut = document.getElementById('nouveauStatut').value;
    try {
        await apiPatch(`/seances/${id}/statut`, { statut });
        showAlert('success', `Statut mis à jour : ${statut}`);
        bootstrap.Modal.getInstance(document.getElementById('statutModal')).hide();
        loadSeances();
    } catch (e) {
        showAlert('danger', e.message);
    }
}

async function deleteSeance(id) {
    if (!confirm('Supprimer cette séance ?')) return;
    try {
        await apiDelete(`/seances/${id}`);
        showAlert('success', 'Séance supprimée');
        loadSeances();
    } catch (e) {
        showAlert('danger', e.message);
    }
}

async function telechargerFacture(id) {
    try {
        await apiDownloadPdf(`/seances/${id}/facture`, `facture-seance-${id}.pdf`);
    } catch (e) {
        showAlert('danger', e.message);
    }
}
