package com.sigma.sahur.studio.model.enums;

/**
 * Statut d'activité d'un photographe dans le système.
 * Seuls les photographes {@code ACTIF} peuvent être affectés à de nouvelles séances.
 */
public enum StatutPhotographe {

    /** Photographe disponible pour prendre en charge des séances. */
    ACTIF,

    /** Photographe désactivé ; ne peut plus être affecté à de nouvelles séances. */
    INACTIF
}
