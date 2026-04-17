package com.sigma.sahur.studio.service;

import com.sigma.sahur.studio.dto.DesactivationResponse;
import com.sigma.sahur.studio.dto.PhotographeRequest;
import com.sigma.sahur.studio.exception.BusinessException;
import com.sigma.sahur.studio.exception.ConflictException;
import com.sigma.sahur.studio.model.Client;
import com.sigma.sahur.studio.model.Photographe;
import com.sigma.sahur.studio.model.Seance;
import com.sigma.sahur.studio.model.enums.Specialite;
import com.sigma.sahur.studio.model.enums.StatutPhotographe;
import com.sigma.sahur.studio.model.enums.StatutSeance;
import com.sigma.sahur.studio.model.enums.TypeSeance;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotographeServiceTest {

    @Mock
    private PhotographeRepository photographeRepository;
    @Mock
    private SeanceRepository seanceRepository;

    @InjectMocks
    private PhotographeService photographeService;

    private Photographe photographe;

    @BeforeEach
    void setUp() {
        photographe = new Photographe();
        photographe.setId(1L);
        photographe.setNom("Alice Martin");
        photographe.setEmail("alice@sigma.fr");
        photographe.setStatut(StatutPhotographe.ACTIF);
        photographe.setSpecialites(Set.of(Specialite.FAMILLE));
    }

    // ─── Création ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Créer un photographe avec email dupliqué doit échouer")
    void testCreerPhotographeEmailDuplique() {
        when(photographeRepository.existsByEmail("alice@sigma.fr")).thenReturn(true);

        PhotographeRequest request = new PhotographeRequest();
        request.setNom("Alice");
        request.setEmail("alice@sigma.fr");
        request.setSpecialites(Set.of(Specialite.FAMILLE));

        assertThatThrownBy(() -> photographeService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("alice@sigma.fr");
    }

    // ─── Désactivation ────────────────────────────────────────────────────

    @Test
    @DisplayName("Désactiver sans séances futures doit désactiver directement")
    void testDesactiverSansSeancesFutures() {
        when(photographeRepository.findById(1L)).thenReturn(Optional.of(photographe));
        when(seanceRepository.findSeancesFuturesActives(eq(1L), any())).thenReturn(List.of());
        when(seanceRepository.findSeancesAAnnnuler(eq(1L), any())).thenReturn(List.of());
        when(photographeRepository.save(any())).thenReturn(photographe);

        DesactivationResponse response = photographeService.changerStatut(1L, StatutPhotographe.INACTIF, false);

        assertThat(response.isRequiresConfirmation()).isFalse();
        assertThat(photographe.getStatut()).isEqualTo(StatutPhotographe.INACTIF);
    }

    @Test
    @DisplayName("Désactiver avec séances futures sans force doit retourner confirmation")
    void testDesactiverAvecSeancesFuturesRetourneConfirmation() {
        when(photographeRepository.findById(1L)).thenReturn(Optional.of(photographe));
        when(seanceRepository.findSeancesFuturesActives(eq(1L), any()))
                .thenReturn(List.of(buildSeanceFuture()));

        DesactivationResponse response = photographeService.changerStatut(1L, StatutPhotographe.INACTIF, false);

        assertThat(response.isRequiresConfirmation()).isTrue();
        assertThat(response.getSeancesFuturesImpactees()).hasSize(1);
        // Le photographe ne doit PAS encore être désactivé
        assertThat(photographe.getStatut()).isEqualTo(StatutPhotographe.ACTIF);
    }

    @Test
    @DisplayName("Désactiver avec force=true doit annuler les séances et désactiver")
    void testDesactiverForceAnnuleSeances() {
        Seance seanceFuture = buildSeanceFuture();
        when(photographeRepository.findById(1L)).thenReturn(Optional.of(photographe));
        when(seanceRepository.findSeancesFuturesActives(eq(1L), any())).thenReturn(List.of(seanceFuture));
        when(seanceRepository.findSeancesAAnnnuler(eq(1L), any())).thenReturn(List.of(seanceFuture));
        when(seanceRepository.saveAll(any())).thenReturn(List.of(seanceFuture));
        when(photographeRepository.save(any())).thenReturn(photographe);

        DesactivationResponse response = photographeService.changerStatut(1L, StatutPhotographe.INACTIF, true);

        assertThat(response.isRequiresConfirmation()).isFalse();
        assertThat(seanceFuture.getStatut()).isEqualTo(StatutSeance.ANNULEE);
        assertThat(photographe.getStatut()).isEqualTo(StatutPhotographe.INACTIF);
    }

    @Test
    @DisplayName("Désactiver un photographe déjà inactif doit échouer")
    void testDesactiverDejaInactifEchoue() {
        photographe.setStatut(StatutPhotographe.INACTIF);
        when(photographeRepository.findById(1L)).thenReturn(Optional.of(photographe));

        assertThatThrownBy(() -> photographeService.changerStatut(1L, StatutPhotographe.INACTIF, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà inactif");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private Seance buildSeanceFuture() {
        Client client = new Client();
        client.setId(1L);
        client.setNom("Dupont");
        client.setPrenom("Jean");

        Seance s = new Seance();
        s.setId(10L);
        s.setStatut(StatutSeance.PLANIFIEE);
        s.setDateSeance(LocalDate.now().plusDays(10));
        s.setHeureDebut(LocalTime.of(10, 0));
        s.setDureeMinutes(120);
        s.setLieu("Studio");
        s.setTypeSeance(TypeSeance.FAMILLE);
        s.setPrix(BigDecimal.valueOf(300));
        s.setPhotographe(photographe);
        s.setClient(client);
        return s;
    }
}
