package com.sigma.sahur.studio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité représentant un client du studio photo.
 * Un client peut être un particulier (prénom + nom) ou une entreprise (raison sociale).
 */
@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
public class Client {

    /** Identifiant unique généré automatiquement. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom de famille du client (particulier) ou dénomination abrégée. */
    @Column(nullable = false, length = 100)
    private String nom;

    /** Prénom du client particulier ; {@code null} pour les entreprises. */
    @Column(length = 100)
    private String prenom;

    /** Raison sociale de l'entreprise cliente ; {@code null} pour les particuliers. */
    @Column(name = "raison_sociale", length = 200)
    private String raisonSociale;

    /** Adresse postale complète du client. */
    @Column(nullable = false, length = 500)
    private String adresse;

    /** Adresse e-mail de contact du client. */
    @Column(nullable = false, length = 150)
    private String email;

    /**
     * Retourne le nom d'affichage du client.
     * Pour une entreprise, retourne la raison sociale ;
     * pour un particulier, retourne "Prénom Nom".
     *
     * @return nom d'affichage non {@code null}
     */
    public String getNomComplet() {
        if (raisonSociale != null && !raisonSociale.isBlank()) {
            return raisonSociale;
        }
        return (prenom != null ? prenom + " " : "") + nom;
    }
}
