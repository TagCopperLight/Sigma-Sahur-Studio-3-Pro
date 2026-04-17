package com.sigma.sahur.studio.controller;

import com.sigma.sahur.studio.dto.*;
import com.sigma.sahur.studio.service.PhotographeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des photographes.
 * Expose les endpoints CRUD et la gestion du statut sous {@code /api/photographes}.
 */
@RestController
@RequestMapping("/api/photographes")
@RequiredArgsConstructor
@Tag(name = "Photographes", description = "Gestion des photographes")
public class PhotographeController {

    private final PhotographeService photographeService;

    /**
     * Retourne la liste de tous les photographes.
     *
     * @return liste des photographes (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Lister tous les photographes")
    public List<PhotographeResponse> getAll() {
        return photographeService.findAll();
    }

    /**
     * Retourne un photographe par son identifiant.
     *
     * @param id identifiant du photographe
     * @return DTO du photographe (HTTP 200)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un photographe par son identifiant")
    public PhotographeResponse getById(@PathVariable Long id) {
        return photographeService.findById(id);
    }

    /**
     * Crée un nouveau photographe.
     *
     * @param request données de création validées
     * @return DTO du photographe créé (HTTP 201)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau photographe")
    public PhotographeResponse create(@Valid @RequestBody PhotographeRequest request) {
        return photographeService.create(request);
    }

    /**
     * Met à jour un photographe existant.
     *
     * @param id      identifiant du photographe à modifier
     * @param request nouvelles données validées
     * @return DTO du photographe mis à jour (HTTP 200)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Modifier un photographe")
    public PhotographeResponse update(@PathVariable Long id,
                                       @Valid @RequestBody PhotographeRequest request) {
        return photographeService.update(id, request);
    }

    /**
     * Supprime un photographe. La suppression est refusée si le photographe a des séances actives.
     *
     * @param id identifiant du photographe à supprimer
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un photographe")
    public void delete(@PathVariable Long id) {
        photographeService.delete(id);
    }

    /**
     * Active ou désactive un photographe.
     *
     * <p>Pour la désactivation, si {@code force=false} (défaut) et des séances futures existent,
     * retourne HTTP 200 avec {@code requiresConfirmation=true} et la liste des séances impactées.
     * Relancer avec {@code ?force=true} pour confirmer l'annulation de ces séances et la désactivation.
     *
     * @param id      identifiant du photographe
     * @param request nouveau statut souhaité
     * @param force   {@code true} pour forcer la désactivation sans nouvelle confirmation
     * @return réponse décrivant le résultat de l'opération (HTTP 200)
     */
    @PatchMapping("/{id}/statut")
    @Operation(summary = "Activer ou désactiver un photographe",
            description = "Pour la désactivation avec séances futures, utiliser ?force=true pour confirmer l'annulation")
    public ResponseEntity<DesactivationResponse> changerStatut(
            @PathVariable Long id,
            @Valid @RequestBody StatutPhotographeRequest request,
            @RequestParam(defaultValue = "false") boolean force) {

        DesactivationResponse response = photographeService.changerStatut(id, request.getStatut(), force);
        return ResponseEntity.ok(response);
    }
}
