/**
 * api.js — Utilitaires fetch pour l'API Sigma Sahur Studio 3 Pro
 * Toutes les fonctions retournent des Promises.
 */

const BASE = '/sigma-sahur-studio-3-pro/api';

async function handleResponse(res) {
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: 'Erreur inconnue' }));
        throw new Error(err.message || `Erreur HTTP ${res.status}`);
    }
    if (res.status === 204) return null;
    return res.json();
}

async function apiGet(path) {
    const res = await fetch(BASE + path);
    return handleResponse(res);
}

async function apiPost(path, body) {
    const res = await fetch(BASE + path, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return handleResponse(res);
}

async function apiPut(path, body) {
    const res = await fetch(BASE + path, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return handleResponse(res);
}

async function apiPatch(path, body) {
    const res = await fetch(BASE + path, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return handleResponse(res);
}

async function apiDelete(path) {
    const res = await fetch(BASE + path, { method: 'DELETE' });
    return handleResponse(res);
}

async function apiDownloadPdf(path, filename) {
    const res = await fetch(BASE + path);
    if (!res.ok) {
        const err = await res.json().catch(() => ({ message: 'Erreur inconnue' }));
        throw new Error(err.message || `Erreur HTTP ${res.status}`);
    }
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

function showAlert(type, message) {
    const container = document.getElementById('alertContainer');
    if (!container) return;
    container.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${escapeHtml(message)}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>`;
    setTimeout(() => {
        const alert = container.querySelector('.alert');
        if (alert) alert.remove();
    }, 5000);
}

function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const [y, m, d] = dateStr.split('-');
    return `${d}/${m}/${y}`;
}

function formatType(type) {
    const map = {
        REPORTAGE: 'Reportage',
        MARIAGE_ET_FETES: 'Mariage et fêtes',
        FAMILLE: 'Famille'
    };
    return map[type] || type;
}

function formatStatut(statut) {
    const map = {
        PLANIFIEE: '<span class="badge bg-secondary">Planifiée</span>',
        CONFIRMEE: '<span class="badge bg-primary">Confirmée</span>',
        TERMINEE:  '<span class="badge bg-success">Terminée</span>',
        ANNULEE:   '<span class="badge bg-danger">Annulée</span>'
    };
    return map[statut] || statut;
}

function formatStatutPhotographe(statut) {
    return statut === 'ACTIF'
        ? '<span class="badge bg-success">Actif</span>'
        : '<span class="badge bg-secondary">Inactif</span>';
}
