package com.sigma.sahur.studio.dto;

import com.sigma.sahur.studio.model.enums.TypeSeance;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Corps de requête pour la création ou la mise à jour d'une séance photo.
 */
@Getter
@Setter
public class SeanceRequest {

    /** Date à laquelle la séance doit avoir lieu. Obligatoire. */
    @NotNull(message = "La date est obligatoire")
    private LocalDate dateSeance;

    /** Heure de début de la séance. Obligatoire. */
    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heureDebut;

    /** Durée de la séance en minutes. Minimum 15 minutes. Obligatoire. */
    @NotNull(message = "La durée est obligatoire")
    @Min(value = 15, message = "La durée minimale est de 15 minutes")
    private Integer dureeMinutes;

    /** Lieu où se déroulera la séance. Obligatoire. */
    @NotBlank(message = "Le lieu est obligatoire")
    private String lieu;

    /** Type de séance (REPORTAGE, MARIAGE_ET_FETES, FAMILLE). Obligatoire. */
    @NotNull(message = "Le type de séance est obligatoire")
    private TypeSeance typeSeance;

    /** Prix de la séance en euros, strictement positif. Obligatoire. */
    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    private BigDecimal prix;

    /** Identifiant du photographe affecté à la séance. Obligatoire. */
    @NotNull(message = "L'identifiant du photographe est obligatoire")
    private Long photographeId;

    /** Identifiant du client ayant réservé la séance. Obligatoire. */
    @NotNull(message = "L'identifiant du client est obligatoire")
    private Long clientId;
}
