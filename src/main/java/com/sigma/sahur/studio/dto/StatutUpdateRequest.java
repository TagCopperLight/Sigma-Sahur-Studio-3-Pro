package com.sigma.sahur.studio.dto;

import com.sigma.sahur.studio.model.enums.StatutSeance;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Corps de requête pour changer le statut d'une séance.
 */
@Getter
@Setter
public class StatutUpdateRequest {

    /** Nouveau statut à appliquer à la séance. Obligatoire. */
    @NotNull(message = "Le statut est obligatoire")
    private StatutSeance statut;
}
