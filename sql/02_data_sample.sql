-- ============================================================
-- Sigma Sahur Studio 3 Pro — Données de test
-- ============================================================

USE info_team02_schema;

-- Photographes
INSERT INTO photographe (nom, email, statut) VALUES
    ('Anais Chemin',  'anais.chemin@sigma-studio.fr',  'ACTIF'),
    ('Baba Boy',   'baba.boy@sigma-studio.fr',   'ACTIF'),
    ('Cristiano Mbappé', 'cristiano.mbappe@sigma-studio.fr', 'ACTIF'),
    ('Didier Pie',  'didier.pie@sigma-studio.fr',  'INACTIF');

-- Spécialités
INSERT INTO photographe_specialite (photographe_id, specialite) VALUES
    (1, 'REPORTAGE'),
    (1, 'FAMILLE'),
    (2, 'MARIAGE_ET_FETES'),
    (2, 'FAMILLE'),
    (3, 'REPORTAGE'),
    (3, 'MARIAGE_ET_FETES'),
    (3, 'FAMILLE'),
    (4, 'REPORTAGE');

-- Clients
INSERT INTO client (nom, prenom, raison_sociale, adresse, email) VALUES
    ('Kempe', 'Adrian',  NULL, '3 route de Treconfitureden, 22300 Lannion', 'adrian.kempe@email.com'),
    ('Mcdavid', 'Connor', NULL, '1 rue du Mazou, 16000 Angouleme', 'connor.mcdavid@email.com'),
    ('Carlsson', NULL,    'Carlsson Events SA','6 avenue de Aled Jesui Bloquet Dans Lescript, 14000 Caen', 'contact@carlsson-events.fr'),
    ('Connor', 'Kyle',  NULL, '9 boulevard du Lac Titicaca, 57140 Woippy', 'kyle.connor@email.com');

-- Séances (dates passées pour pouvoir tester TERMINEE)
INSERT INTO seance (date_seance, heure_debut, duree_minutes, lieu, type_seance, statut, prix, photographe_id, client_id) VALUES
    ('2025-11-10', '09:00:00', 120, 'Studio ENSSAT', 'FAMILLE', 'TERMINEE', 350.00, 1, 1),
    ('2025-12-05', '14:00:00', 240, 'Château de Lannion', 'MARIAGE_ET_FETES', 'TERMINEE', 900.00, 2, 3),
    ('2026-01-20', '10:00:00', 90, 'Parc Saint Anne', 'REPORTAGE', 'TERMINEE', 275.00, 3, 2),
    ('2026-02-14', '11:00:00', 180, 'Studio Marseille bb', 'MARIAGE_ET_FETES', 'TERMINEE', 750.00, 2, 4),
    ('2026-03-01', '09:30:00', 60, 'Studio Photo Cécité', 'FAMILLE', 'TERMINEE', 200.00, 1, 1),
    ('2026-04-20', '10:00:00', 120, 'Studio 4 (le 3 était pris)', 'REPORTAGE', 'CONFIRMEE', 400.00, 3, 3),
    ('2026-05-15', '14:00:00', 240, 'Domaine de Greg', 'MARIAGE_ET_FETES','PLANIFIEE', 950.00, 2, 2);
