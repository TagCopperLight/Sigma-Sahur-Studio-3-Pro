package com.sigma.sahur.studio.controller;

import com.sigma.sahur.studio.dto.TopPhotographeDTO;
import com.sigma.sahur.studio.service.StatistiqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contrôleur REST pour les statistiques du studio.
 * Expose les classements des photographes sous {@code /api/statistiques}.
 */
@RestController
@RequestMapping("/api/statistiques")
@RequiredArgsConstructor
@Tag(name = "Statistiques", description = "Statistiques des photographes")
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    /**
     * Retourne le top 5 des photographes par nombre de séances terminées.
     *
     * @return liste de maximum 5 photographes triés par nombre de séances décroissant (HTTP 200)
     */
    @GetMapping("/top-photographes-count")
    @Operation(summary = "Top 5 photographes par nombre de séances réalisées (TERMINÉES)")
    public List<TopPhotographeDTO> topParNombreSeances() {
        return statistiqueService.getTop5ParNombreSeances();
    }

    /**
     * Retourne le top 5 des photographes par durée cumulée des séances terminées.
     *
     * @return liste de maximum 5 photographes triés par durée cumulée (en minutes) décroissante (HTTP 200)
     */
    @GetMapping("/top-photographes-duree")
    @Operation(summary = "Top 5 photographes par durée cumulée des séances réalisées (TERMINÉES)")
    public List<TopPhotographeDTO> topParDureeCumulee() {
        return statistiqueService.getTop5ParDureeCumulee();
    }
}
