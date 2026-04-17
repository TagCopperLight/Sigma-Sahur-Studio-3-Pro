package com.sigma.sahur.studio.repository;

import com.sigma.sahur.studio.dto.TopPhotographeDTO;
import com.sigma.sahur.studio.model.Seance;
import com.sigma.sahur.studio.model.enums.StatutSeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository JPA pour l'entité {@link Seance}.
 * Fournit les opérations CRUD standard, des requêtes de filtrage et des requêtes JPQL
 * pour les statistiques et la gestion des conflits d'horaires.
 */
@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    /**
     * Retourne toutes les séances ayant le statut donné.
     *
     * @param statut statut de filtrage
     * @return liste des séances correspondantes
     */
    List<Seance> findByStatut(StatutSeance statut);

    /**
     * Retourne toutes les séances affectées à un photographe.
     *
     * @param photographeId identifiant du photographe
     * @return liste des séances du photographe
     */
    List<Seance> findByPhotographeId(Long photographeId);

    /**
     * Retourne toutes les séances d'un client.
     *
     * @param clientId identifiant du client
     * @return liste des séances du client
     */
    List<Seance> findByClientId(Long clientId);

    /**
     * Retourne les séances d'un photographe filtrées par statut.
     *
     * @param photographeId identifiant du photographe
     * @param statut        statut de filtrage
     * @return liste des séances correspondantes
     */
    List<Seance> findByPhotographeIdAndStatut(Long photographeId, StatutSeance statut);

    /**
     * Compte les chevauchements de créneaux pour un photographe donné.
     * Deux séances se chevauchent si : début1 &lt; fin2 ET fin1 &gt; début2.
     * Les séances annulées et la séance elle-même (lors d'une mise à jour) sont exclues.
     *
     * @param photographeId identifiant du photographe
     * @param excludeId     identifiant de la séance à exclure ({@code null} lors d'une création)
     * @param date          date du créneau à tester
     * @param heureDebut    heure de début du créneau à tester
     * @param heureFin      heure de fin du créneau à tester
     * @return nombre de séances en conflit
     */
    @Query("""
            SELECT COUNT(s) FROM Seance s
            WHERE s.photographe.id = :photographeId
              AND (:excludeId IS NULL OR s.id <> :excludeId)
              AND s.statut <> com.sigma.sahur.studio.model.enums.StatutSeance.ANNULEE
              AND s.dateSeance = :date
              AND s.heureDebut < :heureFin
              AND FUNCTION('ADDTIME', s.heureDebut,
                    FUNCTION('SEC_TO_TIME', s.dureeMinutes * 60)) > :heureDebut
            """)
    long countChevauchements(
            @Param("photographeId") Long photographeId,
            @Param("excludeId") Long excludeId,
            @Param("date") LocalDate date,
            @Param("heureDebut") LocalTime heureDebut,
            @Param("heureFin") LocalTime heureFin
    );

    /**
     * Retourne les séances futures actives (PLANIFIEE ou CONFIRMEE) d'un photographe,
     * ordonnées chronologiquement.
     * Utilisé pour afficher les séances impactées lors d'une désactivation.
     *
     * @param photographeId identifiant du photographe
     * @param today         date du jour ; seules les séances postérieures sont retournées
     * @return liste triée des séances futures actives
     */
    @Query("""
            SELECT s FROM Seance s
            WHERE s.photographe.id = :photographeId
              AND s.dateSeance > :today
              AND s.statut IN (
                com.sigma.sahur.studio.model.enums.StatutSeance.PLANIFIEE,
                com.sigma.sahur.studio.model.enums.StatutSeance.CONFIRMEE
              )
            ORDER BY s.dateSeance, s.heureDebut
            """)
    List<Seance> findSeancesFuturesActives(
            @Param("photographeId") Long photographeId,
            @Param("today") LocalDate today
    );

    /**
     * Retourne les séances à annuler lors de la désactivation forcée d'un photographe.
     * Inclut les séances du jour même et futures, avec statut PLANIFIEE ou CONFIRMEE.
     *
     * @param photographeId identifiant du photographe
     * @param today         date du jour ; les séances de ce jour et ultérieures sont incluses
     * @return liste des séances à passer en statut ANNULEE
     */
    @Query("""
            SELECT s FROM Seance s
            WHERE s.photographe.id = :photographeId
              AND s.dateSeance >= :today
              AND s.statut IN (
                com.sigma.sahur.studio.model.enums.StatutSeance.PLANIFIEE,
                com.sigma.sahur.studio.model.enums.StatutSeance.CONFIRMEE
              )
            """)
    List<Seance> findSeancesAAnnnuler(
            @Param("photographeId") Long photographeId,
            @Param("today") LocalDate today
    );

    /**
     * Retourne les 5 photographes ayant le plus grand nombre de séances TERMINÉES.
     *
     * @return liste de maximum 5 DTOs triés par nombre de séances décroissant
     */
    @Query("""
            SELECT new com.sigma.sahur.studio.dto.TopPhotographeDTO(
                s.photographe.id,
                s.photographe.nom,
                COUNT(s)
            )
            FROM Seance s
            WHERE s.statut = com.sigma.sahur.studio.model.enums.StatutSeance.TERMINEE
            GROUP BY s.photographe.id, s.photographe.nom
            ORDER BY COUNT(s) DESC
            LIMIT 5
            """)
    List<TopPhotographeDTO> findTop5ByNombreSeances();

    /**
     * Retourne les 5 photographes ayant la plus grande durée cumulée de séances TERMINÉES.
     *
     * @return liste de maximum 5 DTOs triés par durée cumulée (en minutes) décroissante
     */
    @Query("""
            SELECT new com.sigma.sahur.studio.dto.TopPhotographeDTO(
                s.photographe.id,
                s.photographe.nom,
                SUM(s.dureeMinutes)
            )
            FROM Seance s
            WHERE s.statut = com.sigma.sahur.studio.model.enums.StatutSeance.TERMINEE
            GROUP BY s.photographe.id, s.photographe.nom
            ORDER BY SUM(s.dureeMinutes) DESC
            LIMIT 5
            """)
    List<TopPhotographeDTO> findTop5ByDureeCumulee();

    /**
     * Compte les séances non annulées d'un photographe.
     * Utilisé pour empêcher la suppression d'un photographe ayant des séances actives.
     *
     * @param photographeId identifiant du photographe
     * @return nombre de séances dont le statut n'est pas ANNULEE
     */
    @Query("""
            SELECT COUNT(s) FROM Seance s
            WHERE s.photographe.id = :photographeId
              AND s.statut <> com.sigma.sahur.studio.model.enums.StatutSeance.ANNULEE
            """)
    long countSeancesActivesPhotographe(@Param("photographeId") Long photographeId);

    /**
     * Compte les séances non annulées d'un client.
     * Utilisé pour empêcher la suppression d'un client ayant des séances actives.
     *
     * @param clientId identifiant du client
     * @return nombre de séances dont le statut n'est pas ANNULEE
     */
    @Query("""
            SELECT COUNT(s) FROM Seance s
            WHERE s.client.id = :clientId
              AND s.statut <> com.sigma.sahur.studio.model.enums.StatutSeance.ANNULEE
            """)
    long countSeancesActivesClient(@Param("clientId") Long clientId);
}
