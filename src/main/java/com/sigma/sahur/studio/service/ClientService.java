package com.sigma.sahur.studio.service;

import com.sigma.sahur.studio.dto.ClientRequest;
import com.sigma.sahur.studio.dto.ClientResponse;
import com.sigma.sahur.studio.exception.BusinessException;
import com.sigma.sahur.studio.exception.ResourceNotFoundException;
import com.sigma.sahur.studio.model.Client;
import com.sigma.sahur.studio.repository.ClientRepository;
import com.sigma.sahur.studio.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service métier pour la gestion des clients.
 * Gère les opérations CRUD et vérifie les contraintes d'intégrité (séances actives) avant suppression.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final SeanceRepository seanceRepository;

    /**
     * Retourne la liste de tous les clients.
     *
     * @return liste des clients sous forme de DTOs
     */
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return clientRepository.findAll().stream()
                .map(ClientResponse::new)
                .toList();
    }

    /**
     * Retourne un client par son identifiant.
     *
     * @param id identifiant du client
     * @return DTO du client
     * @throws ResourceNotFoundException si aucun client ne correspond à cet identifiant
     */
    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        return new ClientResponse(getOrThrow(id));
    }

    /**
     * Crée un nouveau client.
     *
     * @param request données de création du client
     * @return DTO du client créé
     */
    public ClientResponse create(ClientRequest request) {
        Client c = new Client();
        applyRequest(c, request);
        return new ClientResponse(clientRepository.save(c));
    }

    /**
     * Met à jour un client existant.
     *
     * @param id      identifiant du client à mettre à jour
     * @param request nouvelles données du client
     * @return DTO du client mis à jour
     * @throws ResourceNotFoundException si aucun client ne correspond à cet identifiant
     */
    public ClientResponse update(Long id, ClientRequest request) {
        Client c = getOrThrow(id);
        applyRequest(c, request);
        return new ClientResponse(clientRepository.save(c));
    }

    /**
     * Supprime un client si et seulement s'il n'a aucune séance active (non annulée).
     *
     * @param id identifiant du client à supprimer
     * @throws ResourceNotFoundException si aucun client ne correspond à cet identifiant
     * @throws BusinessException         si le client possède des séances actives
     */
    public void delete(Long id) {
        getOrThrow(id);
        long seancesActives = seanceRepository.countSeancesActivesClient(id);
        if (seancesActives > 0) {
            throw new BusinessException(
                    "Impossible de supprimer ce client : il a " + seancesActives +
                    " séance(s) active(s). Annulez-les d'abord.");
        }
        clientRepository.deleteById(id);
    }

    private void applyRequest(Client c, ClientRequest request) {
        c.setNom(request.getNom());
        c.setPrenom(request.getPrenom());
        c.setRaisonSociale(request.getRaisonSociale());
        c.setAdresse(request.getAdresse());
        c.setEmail(request.getEmail());
    }

    private Client getOrThrow(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'id " + id));
    }
}
