package com.sigma.sahur.studio.model;

import com.sigma.sahur.studio.model.enums.StatutSeance;
import com.sigma.sahur.studio.model.enums.TypeSeance;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entité représentant une séance photo réservée au studio.
 * Une séance lie un photographe à un client pour un créneau horaire donné.
 */
@Entity
@Table(name = "seance")
@Getter
@Setter
@NoArgsConstructor
public class Seance {

    /** Identifiant unique généré automatiquement. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Date à laquelle la séance est prévue. */
    @Column(name = "date_seance", nullable = false)
    private LocalDate dateSeance;

    /** Heure de début de la séance. */
    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    /** Durée de la séance en minutes (minimum 15). */
    @Column(name = "duree_minutes", nullable = false)
    private Integer dureeMinutes;

    /** Lieu où se déroule la séance. */
    @Column(nullable = false, length = 300)
    private String lieu;

    /** Type de séance déterminant la nature des prestations. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_seance", nullable = false)
    private TypeSeance typeSeance;

    /** Statut courant de la séance dans son cycle de vie ; {@code PLANIFIEE} par défaut. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutSeance statut = StatutSeance.PLANIFIEE;

    /** Prix de la séance en euros (précision 10,2). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    /** Photographe affecté à cette séance. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographe_id", nullable = false)
    private Photographe photographe;

    /** Client ayant réservé cette séance. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * Calcule l'heure de fin de la séance à partir de l'heure de début et de la durée.
     *
     * @return heure de fin calculée
     */
    public LocalTime getHeureFin() {
        return heureDebut.plusMinutes(dureeMinutes);
    }
}
