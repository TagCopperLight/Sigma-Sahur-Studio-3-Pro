/**
 * photographes.js — Gestion de l'onglet Photographes
 */

let editingPhotographeId = null;

async function loadPhotographes() {
    try {
        const data = await apiGet('/photographes');
        const tbody = document.getElementById('photographesTbody');
        if (!data.length) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Aucun photographe</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(p => `
            <tr>
                <td>${escapeHtml(p.nom)}</td>
                <td>${escapeHtml(p.email)}</td>
                <td>${(p.specialites || []).map(s => `<span class="badge bg-info text-dark me-1">${formatType(s)}</span>`).join('')}</td>
                <td>${formatStatutPhotographe(p.statut)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditPhotographe(${p.id})">Éditer</button>
                    <button class="btn btn-sm btn-outline-${p.statut === 'ACTIF' ? 'warning' : 'success'} me-1"
                        onclick="toggleStatutPhotographe(${p.id}, '${p.statut}')">
                        ${p.statut === 'ACTIF' ? 'Désactiver' : 'Activer'}
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deletePhotographe(${p.id})">Supprimer</button>
                </td>
            </tr>`).join('');
    } catch (e) {
        showAlert('danger', e.message);
    }
}

function openCreatePhotographe() {
    editingPhotographeId = null;
    document.getElementById('photographeModalTitle').textContent = 'Nouveau photographe';
    document.getElementById('photographeForm').reset();
    new bootstrap.Modal(document.getElementById('photographeModal')).show();
}

async function openEditPhotographe(id) {
    try {
        const p = await apiGet(`/photographes/${id}`);
        editingPhotographeId = id;
        document.getElementById('photographeModalTitle').textContent = 'Modifier le photographe';
        document.getElementById('photNom').value = p.nom;
        document.getElementById('photEmail').value = p.email;
        document.querySelectorAll('input[name="specialite"]').forEach(cb => {
            cb.checked = (p.specialites || []).includes(cb.value);
        });
        new bootstrap.Modal(document.getElementById('photographeModal')).show();
    } catch (e) {
        showAlert('danger', e.message);
    }
}

async function savePhotographe() {
    const nom = document.getElementById('photNom').value.trim();
    const email = document.getElementById('photEmail').value.trim();
    const specialites = [...document.querySelectorAll('input[name="specialite"]:checked')]
        .map(cb => cb.value);

    if (!nom || !email || !specialites.length) {
        showAlert('warning', 'Veuillez remplir tous les champs et sélectionner au moins une spécialité.');
        return;
    }

    try {
        if (editingPhotographeId) {
            await apiPut(`/photographes/${editingPhotographeId}`, { nom, email, specialites });
            showAlert('success', 'Photographe modifié avec succès');
        } else {
            await apiPost('/photographes', { nom, email, specialites });
            showAlert('success', 'Photographe créé avec succès');
        }
        bootstrap.Modal.getInstance(document.getElementById('photographeModal')).hide();
        loadPhotographes();
    } catch (e) {
        showAlert('danger', e.message);
    }
}

async function deletePhotographe(id) {
    if (!confirm('Supprimer ce photographe ?')) return;
    try {
        await apiDelete(`/photographes/${id}`);
        showAlert('success', 'Photographe supprimé');
        loadPhotographes();
    } catch (e) {
        showAlert('danger', e.message);
    }
}

async function toggleStatutPhotographe(id, statutActuel, force = false) {
    const nouveauStatut = statutActuel === 'ACTIF' ? 'INACTIF' : 'ACTIF';
    try {
        const url = `/photographes/${id}/statut${force ? '?force=true' : ''}`;
        const result = await apiPatch(url, { statut: nouveauStatut });
        if (result.requiresConfirmation) {
            showConfirmationDesactivation(id, result);
        } else {
            showAlert('success', result.message);
            loadPhotographes();
        }
    } catch (e) {
        showAlert('danger', e.message);
    }
}

function showConfirmationDesactivation(id, result) {
    const seancesHtml = result.seancesFuturesImpactees.map(s =>
        `<li>${formatDate(s.dateSeance)} — ${s.lieu} — ${escapeHtml(s.clientNom)}</li>`
    ).join('');

    document.getElementById('confirmDesactivationBody').innerHTML = `
        <p>${escapeHtml(result.message)}</p>
        <p class="fw-bold">Séances qui seront annulées :</p>
        <ul>${seancesHtml}</ul>`;
    document.getElementById('confirmDesactivationBtn').onclick = () => {
        bootstrap.Modal.getInstance(document.getElementById('confirmDesactivationModal')).hide();
        toggleStatutPhotographe(id, 'ACTIF', true);
    };
    new bootstrap.Modal(document.getElementById('confirmDesactivationModal')).show();
}
