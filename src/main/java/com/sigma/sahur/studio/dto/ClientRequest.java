package com.sigma.sahur.studio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Corps de requête pour la création ou la mise à jour d'un client.
 * Un client est soit un particulier (prénom + nom) soit une entreprise (raison sociale).
 */
@Getter
@Setter
public class ClientRequest {

    /** Nom de famille ou dénomination du client. Obligatoire. */
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    /** Prénom du client particulier ; {@code null} pour les entreprises. */
    private String prenom;

    /** Raison sociale de l'entreprise cliente ; {@code null} pour les particuliers. */
    private String raisonSociale;

    /** Adresse postale complète. Obligatoire. */
    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    /** Adresse e-mail de contact. Obligatoire et au format valide. */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    private String email;
}
