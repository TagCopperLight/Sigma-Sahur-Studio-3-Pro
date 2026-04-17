package com.sigma.sahur.studio.controller;

import com.sigma.sahur.studio.dto.SeanceRequest;
import com.sigma.sahur.studio.dto.SeanceResponse;
import com.sigma.sahur.studio.dto.StatutUpdateRequest;
import com.sigma.sahur.studio.model.enums.StatutSeance;
import com.sigma.sahur.studio.service.FactureService;
import com.sigma.sahur.studio.service.SeanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des séances photo.
 * Expose les endpoints CRUD, la gestion des statuts et le téléchargement de factures
 * sous {@code /api/seances}.
 */
@RestController
@RequestMapping("/api/seances")
@RequiredArgsConstructor
@Tag(name = "Séances", description = "Gestion des séances photo")
public class SeanceController {

    private final SeanceService seanceService;
    private final FactureService factureService;

    /**
     * Retourne les séances avec filtres optionnels appliqués en priorité : statut, photographe, client.
     *
     * @param statut        filtre par statut (optionnel)
     * @param photographeId filtre par photographe (optionnel)
     * @param clientId      filtre par client (optionnel)
     * @return liste des séances (HTTP 200)
     */
    @GetMapping
    @Operation(summary = "Lister les séances",
            description = "Filtres optionnels : statut, photographeId, clientId")
    public List<SeanceResponse> getAll(
            @RequestParam(required = false) StatutSeance statut,
            @RequestParam(required = false) Long photographeId,
            @RequestParam(required = false) Long clientId) {
        return seanceService.findAll(statut, photographeId, clientId);
    }

    /**
     * Retourne une séance par son identifiant.
     *
     * @param id identifiant de la séance
     * @return DTO de la séance (HTTP 200)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une séance par son identifiant")
    public SeanceResponse getById(@PathVariable Long id) {
        return seanceService.findById(id);
    }

    /**
     * Crée une nouvelle séance après validation métier.
     *
     * @param request données de création validées
     * @return DTO de la séance créée (HTTP 201)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une nouvelle séance")
    public SeanceResponse create(@Valid @RequestBody SeanceRequest request) {
        return seanceService.create(request);
    }

    /**
     * Met à jour une séance existante (uniquement si PLANIFIEE ou CONFIRMEE).
     *
     * @param id      identifiant de la séance à modifier
     * @param request nouvelles données validées
     * @return DTO de la séance mise à jour (HTTP 200)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Modifier une séance")
    public SeanceResponse update(@PathVariable Long id,
                                  @Valid @RequestBody SeanceRequest request) {
        return seanceService.update(id, request);
    }

    /**
     * Supprime une séance (uniquement si PLANIFIEE ou CONFIRMEE).
     *
     * @param id identifiant de la séance à supprimer
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une séance")
    public void delete(@PathVariable Long id) {
        seanceService.delete(id);
    }

    /**
     * Applique une transition de statut à une séance.
     *
     * @param id      identifiant de la séance
     * @param request nouveau statut cible
     * @return DTO de la séance avec le statut mis à jour (HTTP 200)
     */
    @PatchMapping("/{id}/statut")
    @Operation(summary = "Changer le statut d'une séance",
            description = "Transitions : PLANIFIEE→CONFIRMEE|ANNULEE, CONFIRMEE→TERMINEE(date passée)|ANNULEE")
    public SeanceResponse changerStatut(@PathVariable Long id,
                                         @Valid @RequestBody StatutUpdateRequest request) {
        return seanceService.changerStatut(id, request.getStatut());
    }

    /**
     * Génère et retourne la facture PDF d'une séance terminée en téléchargement.
     *
     * @param id identifiant de la séance terminée
     * @return réponse HTTP avec le contenu PDF en pièce jointe (HTTP 200)
     * @throws IOException en cas d'erreur lors de la génération du PDF
     */
    @GetMapping("/{id}/facture")
    @Operation(summary = "Télécharger la facture PDF d'une séance TERMINÉE")
    public ResponseEntity<byte[]> telechargerFacture(@PathVariable Long id) throws IOException {
        byte[] pdf = factureService.genererFacture(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"facture-seance-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
