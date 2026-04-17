package com.sigma.sahur.studio.dto;

import com.sigma.sahur.studio.model.Seance;
import com.sigma.sahur.studio.model.enums.StatutSeance;
import com.sigma.sahur.studio.model.enums.TypeSeance;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Représentation en lecture seule d'une séance photo, retournée par l'API.
 * L'heure de fin est calculée automatiquement à partir de l'heure de début et de la durée.
 */
@Getter
public class SeanceResponse {

    /** Identifiant unique de la séance. */
    private final Long id;

    /** Date de la séance. */
    private final LocalDate dateSeance;

    /** Heure de début de la séance. */
    private final LocalTime heureDebut;

    /** Heure de fin calculée (heureDebut + dureeMinutes). */
    private final LocalTime heureFin;

    /** Durée de la séance en minutes. */
    private final Integer dureeMinutes;

    /** Lieu de la séance. */
    private final String lieu;

    /** Type de séance. */
    private final TypeSeance typeSeance;

    /** Statut courant de la séance dans son cycle de vie. */
    private final StatutSeance statut;

    /** Prix de la séance en euros. */
    private final BigDecimal prix;

    /** Identifiant du photographe affecté. */
    private final Long photographeId;

    /** Nom du photographe affecté. */
    private final String photographeNom;

    /** Identifiant du client ayant réservé. */
    private final Long clientId;

    /** Nom d'affichage du client. */
    private final String clientNom;

    /**
     * Construit le DTO à partir de l'entité {@link Seance}.
     *
     * @param s entité séance source
     */
    public SeanceResponse(Seance s) {
        this.id = s.getId();
        this.dateSeance = s.getDateSeance();
        this.heureDebut = s.getHeureDebut();
        this.heureFin = s.getHeureFin();
        this.dureeMinutes = s.getDureeMinutes();
        this.lieu = s.getLieu();
        this.typeSeance = s.getTypeSeance();
        this.statut = s.getStatut();
        this.prix = s.getPrix();
        this.photographeId = s.getPhotographe().getId();
        this.photographeNom = s.getPhotographe().getNom();
        this.clientId = s.getClient().getId();
        this.clientNom = s.getClient().getNomComplet();
    }
}
