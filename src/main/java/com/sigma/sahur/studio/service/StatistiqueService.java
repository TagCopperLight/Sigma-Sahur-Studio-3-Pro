package com.sigma.sahur.studio.service;

import com.sigma.sahur.studio.dto.TopPhotographeDTO;
import com.sigma.sahur.studio.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service métier pour les statistiques du studio.
 * Fournit des classements des photographes basés sur les séances terminées.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiqueService {

    private final SeanceRepository seanceRepository;

    /**
     * Retourne le top 5 des photographes ayant réalisé le plus grand nombre de séances terminées.
     *
     * @return liste de maximum 5 DTOs triés par nombre de séances décroissant
     */
    public List<TopPhotographeDTO> getTop5ParNombreSeances() {
        return seanceRepository.findTop5ByNombreSeances();
    }

    /**
     * Retourne le top 5 des photographes ayant la plus grande durée cumulée de séances terminées.
     *
     * @return liste de maximum 5 DTOs triés par durée cumulée (en minutes) décroissante
     */
    public List<TopPhotographeDTO> getTop5ParDureeCumulee() {
        return seanceRepository.findTop5ByDureeCumulee();
    }
}
