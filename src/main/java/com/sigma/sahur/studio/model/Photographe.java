package com.sigma.sahur.studio.model;

import com.sigma.sahur.studio.model.enums.Specialite;
import com.sigma.sahur.studio.model.enums.StatutPhotographe;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un photographe employé par le studio.
 * Un photographe possède une ou plusieurs spécialités et peut être activé ou désactivé.
 */
@Entity
@Table(name = "photographe")
@Getter
@Setter
@NoArgsConstructor
public class Photographe {

    /** Identifiant unique généré automatiquement. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom complet du photographe. */
    @Column(nullable = false, length = 100)
    private String nom;

    /** Adresse e-mail professionnelle, unique dans le système. */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Statut d'activité du photographe ; {@code ACTIF} par défaut à la création. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPhotographe statut = StatutPhotographe.ACTIF;

    /** Ensemble des spécialités photographiques maîtrisées par ce photographe. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "photographe_specialite",
            joinColumns = @JoinColumn(name = "photographe_id")
    )
    @Column(name = "specialite")
    @Enumerated(EnumType.STRING)
    private Set<Specialite> specialites = new HashSet<>();
}
