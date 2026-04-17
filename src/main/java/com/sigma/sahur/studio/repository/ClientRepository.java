package com.sigma.sahur.studio.repository;

import com.sigma.sahur.studio.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA pour l'entité {@link Client}.
 * Fournit les opérations CRUD standard héritées de {@link JpaRepository}.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}
