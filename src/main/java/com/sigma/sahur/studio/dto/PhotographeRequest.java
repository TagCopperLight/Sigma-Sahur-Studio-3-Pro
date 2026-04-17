package com.sigma.sahur.studio.dto;

import com.sigma.sahur.studio.model.enums.Specialite;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Corps de requête pour la création ou la mise à jour d'un photographe.
 */
@Getter
@Setter
public class PhotographeRequest {

    /** Nom complet du photographe. Obligatoire. */
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    /** Adresse e-mail professionnelle unique. Obligatoire et au format valide. */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    private String email;

    /** Ensemble des spécialités du photographe. Au moins une spécialité requise. */
    @NotEmpty(message = "Au moins une spécialité est requise")
    private Set<Specialite> specialites;
}
