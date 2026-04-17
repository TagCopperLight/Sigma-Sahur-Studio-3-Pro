package com.sigma.sahur.studio.service;

import com.sigma.sahur.studio.dto.*;
import com.sigma.sahur.studio.exception.BusinessException;
import com.sigma.sahur.studio.exception.ConflictException;
import com.sigma.sahur.studio.exception.ResourceNotFoundException;
import com.sigma.sahur.studio.model.Photographe;
import com.sigma.sahur.studio.model.Seance;
import com.sigma.sahur.studio.model.enums.StatutPhotographe;
import com.sigma.sahur.studio.model.enums.StatutSeance;
import com.sigma.sahur.studio.repository.PhotographeRepository;
import com.sigma.sahur.studio.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service métier pour la gestion des photographes.
 * Gère les opérations CRUD, l'unicité des e-mails, les contraintes de suppression,
 * et le flux de désactivation avec confirmation lorsque des séances futures existent.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PhotographeService {

    private final PhotographeRepository photographeRepository;
    private final SeanceRepository seanceRepository;

    /**
     * Retourne la liste de tous les photographes.
     *
     * @return liste des photographes sous forme de DTOs
     */
    @Transactional(readOnly = true)
    public List<PhotographeResponse> findAll() {
        return photographeRepository.findAll().stream()
                .map(PhotographeResponse::new)
                .toList();
    }

    /**
     * Retourne un photographe par son identifiant.
     *
     * @param id identifiant du photographe
     * @return DTO du photographe
     * @throws ResourceNotFoundException si aucun photographe ne correspond à cet identifiant
     */
    @Transactional(readOnly = true)
    public PhotographeResponse findById(Long id) {
        return new PhotographeResponse(getOrThrow(id));
    }

    /**
     * Crée un nouveau photographe. L'adresse e-mail doit être unique dans le système.
     *
     * @param request données de création du photographe
     * @return DTO du photographe créé
     * @throws ConflictException si l'e-mail est déjà utilisé par un autre photographe
     */
    public PhotographeResponse create(PhotographeRequest request) {
        if (photographeRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Un photographe avec l'email " + request.getEmail() + " existe déjà");
        }
        Photographe p = new Photographe();
        p.setNom(request.getNom());
        p.setEmail(request.getEmail());
        p.setSpecialites(request.getSpecialites());
        p.setStatut(StatutPhotographe.ACTIF);
        return new PhotographeResponse(photographeRepository.save(p));
    }

    /**
     * Met à jour un photographe existant. Vérifie que le nouvel e-mail n'appartient pas
     * à un autre photographe.
     *
     * @param id      identifiant du photographe à mettre à jour
     * @param request nouvelles données du photographe
     * @return DTO du photographe mis à jour
     * @throws ResourceNotFoundException si aucun photographe ne correspond à cet identifiant
     * @throws ConflictException         si le nouvel e-mail est déjà utilisé par un autre photographe
     */
    public PhotographeResponse update(Long id, PhotographeRequest request) {
        Photographe p = getOrThrow(id);
        if (photographeRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ConflictException("Un autre photographe utilise déjà l'email " + request.getEmail());
        }
        p.setNom(request.getNom());
        p.setEmail(request.getEmail());
        p.setSpecialites(request.getSpecialites());
        return new PhotographeResponse(photographeRepository.save(p));
    }

    /**
     * Supprime un photographe si et seulement s'il n'a aucune séance active (non annulée).
     *
     * @param id identifiant du photographe à supprimer
     * @throws ResourceNotFoundException si aucun photographe ne correspond à cet identifiant
     * @throws BusinessException         si le photographe possède des séances actives
     */
    public void delete(Long id) {
        getOrThrow(id);
        long seancesActives = seanceRepository.countSeancesActivesPhotographe(id);
        if (seancesActives > 0) {
            throw new BusinessException(
                    "Impossible de supprimer ce photographe : il a " + seancesActives +
                    " séance(s) active(s). Annulez-les d'abord.");
        }
        photographeRepository.deleteById(id);
    }

    /**
     * Gère l'activation ou la désactivation d'un photographe.
     *
     * <p>Pour l'activation : le statut passe directement à ACTIF.
     *
     * <p>Pour la désactivation :
     * <ul>
     *   <li>Si {@code force=false} et des séances futures (PLANIFIEE ou CONFIRMEE) existent :
     *       retourne une réponse avec {@code requiresConfirmation=true} listant les séances impactées.</li>
     *   <li>Si {@code force=true} ou aucune séance future : annule les séances futures concernées
     *       et passe le photographe à INACTIF.</li>
     * </ul>
     *
     * @param id            identifiant du photographe
     * @param nouveauStatut nouveau statut souhaité (ACTIF ou INACTIF)
     * @param force         {@code true} pour forcer la désactivation sans confirmation
     * @return réponse décrivant le résultat de l'opération
     * @throws ResourceNotFoundException si aucun photographe ne correspond à cet identifiant
     * @throws BusinessException         si le photographe est déjà dans le statut demandé (INACTIF → INACTIF)
     */
    public DesactivationResponse changerStatut(Long id, StatutPhotographe nouveauStatut, boolean force) {
        Photographe p = getOrThrow(id);

        if (nouveauStatut == StatutPhotographe.ACTIF) {
            p.setStatut(StatutPhotographe.ACTIF);
            photographeRepository.save(p);
            return new DesactivationResponse(false, "Photographe activé avec succès", List.of(),
                    new PhotographeResponse(p));
        }

        // Désactivation
        if (p.getStatut() == StatutPhotographe.INACTIF) {
            throw new BusinessException("Le photographe est déjà inactif");
        }

        List<Seance> seancesFutures = seanceRepository.findSeancesFuturesActives(id, LocalDate.now());

        if (!seancesFutures.isEmpty() && !force) {
            // Demande de confirmation
            List<SeanceResponse> seancesDto = seancesFutures.stream()
                    .map(SeanceResponse::new)
                    .toList();
            return new DesactivationResponse(true,
                    "Ce photographe a " + seancesFutures.size() +
                    " séance(s) à venir. Confirmez pour les annuler et désactiver le photographe.",
                    seancesDto, new PhotographeResponse(p));
        }

        // Annuler toutes les séances futures et désactiver
        List<Seance> aAnnuler = seanceRepository.findSeancesAAnnnuler(id, LocalDate.now());
        aAnnuler.forEach(s -> s.setStatut(StatutSeance.ANNULEE));
        seanceRepository.saveAll(aAnnuler);

        p.setStatut(StatutPhotographe.INACTIF);
        photographeRepository.save(p);

        return new DesactivationResponse(false,
                "Photographe désactivé. " + aAnnuler.size() + " séance(s) annulée(s).",
                List.of(), new PhotographeResponse(p));
    }

    private Photographe getOrThrow(Long id) {
        return photographeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Photographe introuvable avec l'id " + id));
    }
}
