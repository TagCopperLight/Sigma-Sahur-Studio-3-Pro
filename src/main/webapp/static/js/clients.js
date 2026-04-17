/**
 * clients.js — Gestion de l'onglet Clients
 */

let editingClientId = null;

async function loadClients() {
    try {
        const data = await apiGet('/clients');
        const tbody = document.getElementById('clientsTbody');
        if (!data.length) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">Aucun client</td></tr>';
            return;
        }
        tbody.innerHTML = data.map(c => `
            <tr>
                <td>${escapeHtml(c.nomComplet)}</td>
                <td>${escapeHtml(c.adresse)}</td>
                <td>${escapeHtml(c.email)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="openEditClient(${c.id})">Éditer</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteClient(${c.id})">Supprimer</button>
                </td>
            </tr>`).join('');
    } catch (e) {
        showAlert('danger', e.message);
    }
}

function openCreateClient() {
    editingClientId = null;
    document.getElementById('clientModalTitle').textContent = 'Nouveau client';
    document.getElementById('clientForm').reset();
    new bootstrap.Modal(document.getElementById('clientModal')).show();
}

async function openEditClient(id) {
    try {
        const c = await apiGet(`/clients/${id}`);
        editingClientId = id;
        document.getElementById('clientModalTitle').textContent = 'Modifier le client';
        document.getElementById('clientNom').value = c.nom || '';
        document.getElementById('clientPrenom').value = c.prenom || '';
        document.getElementById('clientRaisonSociale').value = c.raisonSociale || '';
        document.getElementById('clientAdresse').value = c.adresse || '';
        document.getElementById('clientEmail').value = c.email || '';
        new bootstrap.Modal(document.getElementById('clientModal')).show();
    } catch (e) {
        showAlert('danger', e.message);
    }
}

async function saveClient() {
    const body = {
        nom:           document.getElementById('clientNom').value.trim(),
        prenom:        document.getElementById('clientPrenom').value.trim() || null,
        raisonSociale: document.getElementById('clientRaisonSociale').value.trim() || null,
        adresse:       document.getElementById('clientAdresse').value.trim(),
        email:         document.getElementById('clientEmail').value.trim()
    };

    if (!body.nom || !body.adresse || !body.email) {
        showAlert('warning', 'Nom, adresse et email sont obligatoires.');
        return;
    }

    try {
        if (editingClientId) {
            await apiPut(`/clients/${editingClientId}`, body);
            showAlert('success', 'Client modifié avec succès');
        } else {
            await apiPost('/clients', body);
            showAlert('success', 'Client créé avec succès');
        }
        bootstrap.Modal.getInstance(document.getElementById('clientModal')).hide();
        loadClients();
    } catch (e) {
        showAlert('danger', e.message);
    }
}

async function deleteClient(id) {
    if (!confirm('Supprimer ce client ?')) return;
    try {
        await apiDelete(`/clients/${id}`);
        showAlert('success', 'Client supprimé');
        loadClients();
    } catch (e) {
        showAlert('danger', e.message);
    }
}
