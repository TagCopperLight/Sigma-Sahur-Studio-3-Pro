/**
 * statistiques.js — Gestion de l'onglet Statistiques
 */

async function loadStatistiques() {
    await Promise.all([
        loadTopParCount(),
        loadTopParDuree()
    ]);
}

async function loadTopParCount() {
    try {
        const data = await apiGet('/statistiques/top-photographes-count');
        const tbody = document.getElementById('statsCountTbody');
        if (!data.length) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">Aucune donnée</td></tr>';
            return;
        }
        tbody.innerHTML = data.map((p, i) => `
            <tr>
                <td><span class="badge bg-warning text-dark">#${i + 1}</span></td>
                <td>${escapeHtml(p.photographeNom)}</td>
                <td>${p.valeur} séance${p.valeur > 1 ? 's' : ''}</td>
            </tr>`).join('');
    } catch (e) {
        showAlert('danger', e.message);
    }
}

async function loadTopParDuree() {
    try {
        const data = await apiGet('/statistiques/top-photographes-duree');
        const tbody = document.getElementById('statsDureeTbody');
        if (!data.length) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">Aucune donnée</td></tr>';
            return;
        }
        tbody.innerHTML = data.map((p, i) => `
            <tr>
                <td><span class="badge bg-warning text-dark">#${i + 1}</span></td>
                <td>${escapeHtml(p.photographeNom)}</td>
                <td>${formatDuree(p.valeur)}</td>
            </tr>`).join('');
    } catch (e) {
        showAlert('danger', e.message);
    }
}

function formatDuree(minutes) {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h === 0) return `${m} min`;
    if (m === 0) return `${h}h`;
    return `${h}h ${m}min`;
}
