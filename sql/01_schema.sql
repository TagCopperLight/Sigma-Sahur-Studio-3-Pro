USE info_team02_schema;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS seance;
DROP TABLE IF EXISTS photographe_specialite;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS photographe;

SET FOREIGN_KEY_CHECKS = 1;

-- Table : photographe
CREATE TABLE photographe (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom    VARCHAR(100) NOT NULL,
    email  VARCHAR(150) NOT NULL UNIQUE,
    statut ENUM('ACTIF', 'INACTIF') NOT NULL DEFAULT 'ACTIF'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : photographe_specialite
CREATE TABLE photographe_specialite (
    photographe_id BIGINT NOT NULL,
    specialite     ENUM('REPORTAGE', 'MARIAGE_ET_FETES', 'FAMILLE') NOT NULL,
    PRIMARY KEY (photographe_id, specialite),
    CONSTRAINT fk_ps_photographe FOREIGN KEY (photographe_id)
        REFERENCES photographe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : client
CREATE TABLE client (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom            VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100)  DEFAULT NULL,  -- NULL pour les entreprises
    raison_sociale VARCHAR(200)  DEFAULT NULL,  -- NULL pour les particuliers
    adresse        VARCHAR(500) NOT NULL,
    email          VARCHAR(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table : seance
CREATE TABLE seance (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_seance    DATE         NOT NULL,
    heure_debut    TIME         NOT NULL,
    duree_minutes  INT          NOT NULL,              -- durée en minutes
    lieu           VARCHAR(300) NOT NULL,
    type_seance    ENUM('REPORTAGE', 'MARIAGE_ET_FETES', 'FAMILLE') NOT NULL,
    statut         ENUM('PLANIFIEE', 'CONFIRMEE', 'TERMINEE', 'ANNULEE') NOT NULL DEFAULT 'PLANIFIEE',
    prix           DECIMAL(10, 2) NOT NULL,
    photographe_id BIGINT       NOT NULL,
    client_id      BIGINT       NOT NULL,
    CONSTRAINT fk_seance_photographe FOREIGN KEY (photographe_id)
        REFERENCES photographe (id),
    CONSTRAINT fk_seance_client FOREIGN KEY (client_id)
        REFERENCES client (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index pour accélérer les recherches fréquentes
CREATE INDEX idx_seance_photographe_date ON seance (photographe_id, date_seance);
CREATE INDEX idx_seance_statut           ON seance (statut);
CREATE INDEX idx_seance_client           ON seance (client_id);
