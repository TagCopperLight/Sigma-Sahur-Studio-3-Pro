package com.sigma.sahur.studio.service;

import com.sigma.sahur.studio.dto.SeanceRequest;
import com.sigma.sahur.studio.dto.SeanceResponse;
import com.sigma.sahur.studio.exception.BusinessException;
import com.sigma.sahur.studio.exception.ConflictException;
import com.sigma.sahur.studio.exception.ResourceNotFoundException;
import com.sigma.sahur.studio.model.Client;
import com.sigma.sahur.studio.model.Photographe;
import com.sigma.sahur.studio.model.Seance;
import com.sigma.sahur.studio.model.enums.StatutPhotographe;
import com.sigma.sahur.studio.model.enums.StatutSeance;
import com.sigma.sahur.studio.repository.ClientRepository;
import com.sigma.sahur.studio.repository.PhotographeRepository;
import com.sigma.sahur.studio.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service métier pour la gestion des séances photo.
 * Implémente les règles de validation (date future, photographe actif, absence de chevauchement),
 * la machine à états des statuts, et les contraintes de suppression.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final PhotographeRepository photographeRepository;
    private final ClientRepository clientRepository;

    /**
     * Retourne les séances selon des filtres optionnels.
     * Les filtres sont appliqués dans l'ordre de priorité : statut, photographe, client.
     * Si aucun filtre n'est fourni, toutes les séances sont retournées.
     *
     * @param statut        filtre par statut (optionnel)
     * @param photographeId filtre par photographe (optionnel)
     * @param clientId      filtre par client (optionnel)
     * @return liste des séances correspondantes
     */
    @Transactional(readOnly = true)
    public List<SeanceResponse> findAll(StatutSeance statut, Long photographeId, Long clientId) {
        List<Seance> seances;
        if (statut != null) {
            seances = seanceRepository.findByStatut(statut);
        } else if (photographeId != null) {
            seances = seanceRepository.findByPhotographeId(photographeId);
        } else if (clientId != null) {
            seances = seanceRepository.findByClientId(clientId);
        } else {
            seances = seanceRepository.findAll();
        }
        return seances.stream().map(SeanceResponse::new).toList();
    }

    /**
     * Retourne une séance par son identifiant.
     *
     * @param id identifiant de la séance
     * @return DTO de la séance
     * @throws ResourceNotFoundException si aucune séance ne correspond à cet identifiant
     */
    @Transactional(readOnly = true)
    public SeanceResponse findById(Long id) {
        return new SeanceResponse(getOrThrow(id));
    }

    /**
     * Crée une nouvelle séance après validation des règles métier.
     *
     * @param request données de création de la séance
     * @return DTO de la séance créée
     * @throws BusinessException         si la date est passée ou si le photographe est inactif
     * @throws ConflictException         si un chevauchement de créneau est détecté
     * @throws ResourceNotFoundException si le photographe ou le client est introuvable
     */
    public SeanceResponse create(SeanceRequest request) {
        validerSeance(request, null);
        Seance s = new Seance();
        applyRequest(s, request);
        return new SeanceResponse(seanceRepository.save(s));
    }

    /**
     * Met à jour une séance existante. Seules les séances PLANIFIEE et CONFIRMEE peuvent être modifiées.
     *
     * @param id      identifiant de la séance à mettre à jour
     * @param request nouvelles données de la séance
     * @return DTO de la séance mise à jour
     * @throws ResourceNotFoundException si aucune séance ne correspond à cet identifiant
     * @throws BusinessException         si la séance est TERMINEE ou ANNULEE, ou si la règle métier est violée
     * @throws ConflictException         si un chevauchement de créneau est détecté
     */
    public SeanceResponse update(Long id, SeanceRequest request) {
        Seance s = getOrThrow(id);
        verifierModifiable(s);
        validerSeance(request, id);
        applyRequest(s, request);
        return new SeanceResponse(seanceRepository.save(s));
    }

    /**
     * Supprime une séance. Seules les séances PLANIFIEE et CONFIRMEE peuvent être supprimées.
     *
     * @param id identifiant de la séance à supprimer
     * @throws ResourceNotFoundException si aucune séance ne correspond à cet identifiant
     * @throws BusinessException         si la séance est TERMINEE ou ANNULEE
     */
    public void delete(Long id) {
        Seance s = getOrThrow(id);
        verifierModifiable(s);
        seanceRepository.deleteById(id);
    }

    /**
     * Applique une transition de statut à une séance selon la machine à états suivante :
     * <ul>
     *   <li>PLANIFIEE → CONFIRMEE ou ANNULEE</li>
     *   <li>CONFIRMEE → TERMINEE (uniquement si la date de séance est passée) ou ANNULEE</li>
     *   <li>TERMINEE et ANNULEE → immuables</li>
     * </ul>
     *
     * @param id            identifiant de la séance
     * @param nouveauStatut statut cible
     * @return DTO de la séance avec le nouveau statut
     * @throws ResourceNotFoundException si aucune séance ne correspond à cet identifiant
     * @throws BusinessException         si la transition demandée n'est pas autorisée
     */
    public SeanceResponse changerStatut(Long id, StatutSeance nouveauStatut) {
        Seance s = getOrThrow(id);
        StatutSeance actuel = s.getStatut();

        switch (actuel) {
            case PLANIFIEE -> {
                if (nouveauStatut != StatutSeance.CONFIRMEE && nouveauStatut != StatutSeance.ANNULEE) {
                    throw new BusinessException(
                            "Une séance PLANIFIÉE ne peut passer qu'à CONFIRMÉE ou ANNULÉE");
                }
            }
            case CONFIRMEE -> {
                if (nouveauStatut == StatutSeance.TERMINEE) {
                    if (!s.getDateSeance().isBefore(LocalDate.now())) {
                        throw new BusinessException(
                                "Une séance ne peut être terminée que si sa date est passée");
                    }
                } else if (nouveauStatut != StatutSeance.ANNULEE) {
                    throw new BusinessException(
                            "Une séance CONFIRMÉE ne peut passer qu'à TERMINÉE ou ANNULÉE");
                }
            }
            case TERMINEE -> throw new BusinessException("Une séance TERMINÉE ne peut plus être modifiée");
            case ANNULEE  -> throw new BusinessException("Une séance ANNULÉE ne peut plus être modifiée");
        }

        s.setStatut(nouveauStatut);
        return new SeanceResponse(seanceRepository.save(s));
    }

    // -------------------------------------------------------------------------
    // Méthodes privées
    // -------------------------------------------------------------------------

    private void validerSeance(SeanceRequest request, Long excludeId) {
        // 1. La séance doit être dans le futur
        LocalDateTime debut = LocalDateTime.of(request.getDateSeance(), request.getHeureDebut());
        if (!debut.isAfter(LocalDateTime.now())) {
            throw new BusinessException("Il est impossible de planifier une séance dans le passé");
        }

        // 2. Le photographe doit être actif
        Photographe photographe = photographeRepository.findById(request.getPhotographeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Photographe introuvable avec l'id " + request.getPhotographeId()));
        if (photographe.getStatut() == StatutPhotographe.INACTIF) {
            throw new BusinessException(
                    "Le photographe " + photographe.getNom() + " est inactif et ne peut pas être assigné à une séance");
        }

        // 3. Vérification de chevauchement
        LocalTime heureFin = request.getHeureDebut().plusMinutes(request.getDureeMinutes());
        long chevauchements = seanceRepository.countChevauchements(
                request.getPhotographeId(),
                excludeId,
                request.getDateSeance(),
                request.getHeureDebut(),
                heureFin
        );
        if (chevauchements > 0) {
            throw new ConflictException(
                    "Le photographe " + photographe.getNom() +
                    " a déjà une séance qui se chevauche sur ce créneau");
        }
    }

    private void applyRequest(Seance s, SeanceRequest request) {
        s.setDateSeance(request.getDateSeance());
        s.setHeureDebut(request.getHeureDebut());
        s.setDureeMinutes(request.getDureeMinutes());
        s.setLieu(request.getLieu());
        s.setTypeSeance(request.getTypeSeance());
        s.setPrix(request.getPrix());

        Photographe photographe = photographeRepository.findById(request.getPhotographeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Photographe introuvable avec l'id " + request.getPhotographeId()));
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client introuvable avec l'id " + request.getClientId()));

        s.setPhotographe(photographe);
        s.setClient(client);
    }

    private void verifierModifiable(Seance s) {
        if (s.getStatut() == StatutSeance.TERMINEE || s.getStatut() == StatutSeance.ANNULEE) {
            throw new BusinessException(
                    "Une séance " + s.getStatut() + " ne peut plus être modifiée");
        }
    }

    private Seance getOrThrow(Long id) {
        return seanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Séance introuvable avec l'id " + id));
    }
}
