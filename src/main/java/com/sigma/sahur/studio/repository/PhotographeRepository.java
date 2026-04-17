package com.sigma.sahur.studio.repository;

import com.sigma.sahur.studio.model.Photographe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité {@link Photographe}.
 * Fournit les opérations CRUD standard et des requêtes de recherche par e-mail.
 */
@Repository
public interface PhotographeRepository extends JpaRepository<Photographe, Long> {

    /**
     * Recherche un photographe par son adresse e-mail.
     *
     * @param email adresse e-mail à rechercher
     * @return un {@link Optional} contenant le photographe s'il existe
     */
    Optional<Photographe> findByEmail(String email);

    /**
     * Vérifie si un photographe avec cet e-mail existe déjà.
     *
     * @param email adresse e-mail à tester
     * @return {@code true} si l'e-mail est déjà utilisé
     */
    boolean existsByEmail(String email);

    /**
     * Vérifie si un e-mail est utilisé par un autre photographe (contrôle lors d'une mise à jour).
     *
     * @param email adresse e-mail à tester
     * @param id    identifiant du photographe à exclure de la recherche
     * @return {@code true} si l'e-mail appartient à un autre photographe
     */
    boolean existsByEmailAndIdNot(String email, Long id);
}
