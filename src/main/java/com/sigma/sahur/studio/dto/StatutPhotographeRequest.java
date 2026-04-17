package com.sigma.sahur.studio.dto;

import com.sigma.sahur.studio.model.enums.StatutPhotographe;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Corps de requête pour changer le statut d'activité d'un photographe.
 */
@Getter
@Setter
public class StatutPhotographeRequest {

    /** Nouveau statut à appliquer au photographe (ACTIF ou INACTIF). Obligatoire. */
    @NotNull(message = "Le statut est obligatoire")
    private StatutPhotographe statut;
}
