package com.sigma.sahur.studio.dto;

import com.sigma.sahur.studio.model.Photographe;
import com.sigma.sahur.studio.model.enums.Specialite;
import com.sigma.sahur.studio.model.enums.StatutPhotographe;
import lombok.Getter;

import java.util.Set;

/**
 * Représentation en lecture seule d'un photographe, retournée par l'API.
 */
@Getter
public class PhotographeResponse {

    /** Identifiant unique du photographe. */
    private final Long id;

    /** Nom complet du photographe. */
    private final String nom;

    /** Adresse e-mail professionnelle. */
    private final String email;

    /** Statut d'activité courant (ACTIF ou INACTIF). */
    private final StatutPhotographe statut;

    /** Spécialités photographiques du photographe. */
    private final Set<Specialite> specialites;

    /**
     * Construit le DTO à partir de l'entité {@link Photographe}.
     *
     * @param p entité photographe source
     */
    public PhotographeResponse(Photographe p) {
        this.id = p.getId();
        this.nom = p.getNom();
        this.email = p.getEmail();
        this.statut = p.getStatut();
        this.specialites = p.getSpecialites();
    }
}
