package com.sigma.sahur.studio.controller;

import com.sigma.sahur.studio.dto.ClientRequest;
import com.sigma.sahur.studio.dto.ClientResponse;
import com.sigma.sahur.studio.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des clients.
 * Expose les endpoints CRUD sous {@code /api/clients}.
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Gestion des clients")
public class ClientController {

    private final ClientService clientService;

    /**
     * Retourne la liste de tous les clients.
     *
     * @return liste des clients (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Lister tous les clients")
    public List<ClientResponse> getAll() {
        return clientService.findAll();
    }

    /**
     * Retourne un client par son identifiant.
     *
     * @param id identifiant du client
     * @return DTO du client (HTTP 200)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un client par son identifiant")
    public ClientResponse getById(@PathVariable Long id) {
        return clientService.findById(id);
    }

    /**
     * Crée un nouveau client.
     *
     * @param request données de création validées
     * @return DTO du client créé (HTTP 201)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau client")
    public ClientResponse create(@Valid @RequestBody ClientRequest request) {
        return clientService.create(request);
    }

    /**
     * Met à jour un client existant.
     *
     * @param id      identifiant du client à modifier
     * @param request nouvelles données validées
     * @return DTO du client mis à jour (HTTP 200)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un client")
    public ClientResponse update(@PathVariable Long id,
                                  @Valid @RequestBody ClientRequest request) {
        return clientService.update(id, request);
    }

    /**
     * Supprime un client. La suppression est refusée si le client possède des séances actives.
     *
     * @param id identifiant du client à supprimer
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un client")
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }
}
