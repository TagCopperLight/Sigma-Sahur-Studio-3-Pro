package com.sigma.sahur.studio.service;

import com.sigma.sahur.studio.dto.SeanceRequest;
import com.sigma.sahur.studio.dto.SeanceResponse;
import com.sigma.sahur.studio.exception.BusinessException;
import com.sigma.sahur.studio.exception.ConflictException;
import com.sigma.sahur.studio.model.Client;
import com.sigma.sahur.studio.model.Photographe;
import com.sigma.sahur.studio.model.Seance;
import com.sigma.sahur.studio.model.enums.StatutPhotographe;
import com.sigma.sahur.studio.model.enums.StatutSeance;
import com.sigma.sahur.studio.model.enums.TypeSeance;
import com.sigma.sahur.studio.repository.ClientRepository;
import com.sigma.sahur.studio.repository.PhotographeRepository;
import com.sigma.sahur.studio.repository.SeanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeanceServiceTest {

    @Mock
    private SeanceRepository seanceRepository;
    @Mock
    private PhotographeRepository photographeRepository;
    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private SeanceService seanceService;

    private Photographe photographeActif;
    private Photographe photographeInactif;
    private Client client;

    @BeforeEach
    void setUp() {
        photographeActif = new Photographe();
        photographeActif.setId(1L);
        photographeActif.setNom("Alice");
        photographeActif.setStatut(StatutPhotographe.ACTIF);

        photographeInactif = new Photographe();
        photographeInactif.setId(2L);
        photographeInactif.setNom("Bob");
        photographeInactif.setStatut(StatutPhotographe.INACTIF);

        client = new Client();
        client.setId(1L);
        client.setNom("Dupont");
        client.setPrenom("Jean");
    }

    // ─── Création ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Créer une séance dans le passé doit échouer")
    void testCreerSeanceDansLePasseEchoue() {
        SeanceRequest request = buildRequest(LocalDate.now().minusDays(1), LocalTime.of(10, 0));

        // La validation de date intervient avant tout accès au repository
        assertThatThrownBy(() -> seanceService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("passé");
    }

    @Test
    @DisplayName("Créer une séance avec un photographe inactif doit échouer")
    void testCreerSeancePhotographeInactifEchoue() {
        SeanceRequest request = buildRequest(LocalDate.now().plusDays(7), LocalTime.of(10, 0));
        request.setPhotographeId(2L);
        when(photographeRepository.findById(2L)).thenReturn(Optional.of(photographeInactif));

        assertThatThrownBy(() -> seanceService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactif");
    }

    @Test
    @DisplayName("Créer une séance avec chevauchement doit échouer")
    void testCreerSeanceChevauchementEchoue() {
        SeanceRequest request = buildRequest(LocalDate.now().plusDays(7), LocalTime.of(10, 0));
        when(photographeRepository.findById(1L)).thenReturn(Optional.of(photographeActif));
        when(seanceRepository.countChevauchements(anyLong(), isNull(), any(), any(), any()))
                .thenReturn(1L);

        assertThatThrownBy(() -> seanceService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("chevauche");
    }

    // ─── Changement de statut ──────────────────────────────────────────────

    @Test
    @DisplayName("Transition PLANIFIEE → CONFIRMEE doit réussir")
    void testTransitionStatutValide() {
        Seance seance = buildSeance(StatutSeance.PLANIFIEE, LocalDate.now().plusDays(5));
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));
        when(seanceRepository.save(any())).thenReturn(seance);

        SeanceResponse response = seanceService.changerStatut(1L, StatutSeance.CONFIRMEE);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Transition TERMINEE → PLANIFIEE doit échouer")
    void testTransitionStatutInvalide() {
        Seance seance = buildSeance(StatutSeance.TERMINEE, LocalDate.now().minusDays(5));
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));

        assertThatThrownBy(() -> seanceService.changerStatut(1L, StatutSeance.PLANIFIEE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("TERMINÉE");
    }

    @Test
    @DisplayName("Terminer une séance à date future doit échouer")
    void testTerminerSeanceDateFutureEchoue() {
        Seance seance = buildSeance(StatutSeance.CONFIRMEE, LocalDate.now().plusDays(3));
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));

        assertThatThrownBy(() -> seanceService.changerStatut(1L, StatutSeance.TERMINEE))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("passée");
    }

    @Test
    @DisplayName("Terminer une séance à date passée doit réussir")
    void testTerminerSeanceDatePasseeReussit() {
        Seance seance = buildSeance(StatutSeance.CONFIRMEE, LocalDate.now().minusDays(1));
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));
        when(seanceRepository.save(any())).thenReturn(seance);

        SeanceResponse response = seanceService.changerStatut(1L, StatutSeance.TERMINEE);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Modifier une séance ANNULEE doit échouer")
    void testModifierSeanceAnnuleeEchoue() {
        Seance seance = buildSeance(StatutSeance.ANNULEE, LocalDate.now().plusDays(5));
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));

        SeanceRequest request = buildRequest(LocalDate.now().plusDays(7), LocalTime.of(14, 0));
        assertThatThrownBy(() -> seanceService.update(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ANNULEE");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private SeanceRequest buildRequest(LocalDate date, LocalTime heure) {
        SeanceRequest r = new SeanceRequest();
        r.setDateSeance(date);
        r.setHeureDebut(heure);
        r.setDureeMinutes(120);
        r.setLieu("Studio Test");
        r.setTypeSeance(TypeSeance.FAMILLE);
        r.setPrix(BigDecimal.valueOf(300));
        r.setPhotographeId(1L);
        r.setClientId(1L);
        return r;
    }

    private Seance buildSeance(StatutSeance statut, LocalDate date) {
        Seance s = new Seance();
        s.setId(1L);
        s.setStatut(statut);
        s.setDateSeance(date);
        s.setHeureDebut(LocalTime.of(10, 0));
        s.setDureeMinutes(120);
        s.setLieu("Studio Test");
        s.setTypeSeance(TypeSeance.FAMILLE);
        s.setPrix(BigDecimal.valueOf(300));
        s.setPhotographe(photographeActif);
        s.setClient(client);
        return s;
    }
}
