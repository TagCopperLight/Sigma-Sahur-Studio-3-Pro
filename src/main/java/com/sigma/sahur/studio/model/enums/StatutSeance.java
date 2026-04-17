package com.sigma.sahur.studio.model.enums;

/**
 * Cycle de vie d'une séance photo.
 * Les transitions valides sont : PLANIFIEE → CONFIRMEE → TERMINEE,
 * ou PLANIFIEE/CONFIRMEE → ANNULEE.
 */
public enum StatutSeance {

    /** Séance créée mais pas encore confirmée par le client ou le studio. */
    PLANIFIEE,

    /** Séance confirmée ; les deux parties sont engagées. */
    CONFIRMEE,

    /** Séance réalisée ; une facture peut être générée. */
    TERMINEE,

    /** Séance annulée ; ne peut plus être modifiée. */
    ANNULEE
}
