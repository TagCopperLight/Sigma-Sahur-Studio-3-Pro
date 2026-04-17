# Rapport technique — Sigma Sahur Studio 3 Pro

**Auteurs :** Nicolas DEVOS, Malo JOANNON, Lila HAMDI, Pierre-Louis ROMAND, Julien BOURDET

---

## Table des matières

1. [Contexte et objectifs](#1-contexte-et-objectifs)
2. [Choix technologiques](#2-choix-technologiques)
3. [Architecture de l'application](#3-architecture-de-lapplication)
4. [Modèle de données](#4-modèle-de-données)
5. [API REST](#5-api-rest)
6. [Tests unitaires](#6-tests-unitaires)
7. [Livrables et procédure d'installation](#7-livrables-et-procédure-dinstallation)
8. [Conclusion](#8-conclusion)

---

## 1. Contexte et objectifs

Un studio de photographie professionnel souhaitait remplacer une gestion manuelle (plannings papier, facturation Excel) par un système centralisé. Les problèmes identifiés étaient : les conflits de planning entre photographes, les pertes d'informations clients et l'absence de traçabilité des séances.

L'objectif était de livrer une application web complète permettant de gérer les photographes, les clients, les séances photo et la facturation, en imposant des règles métier strictes garantissant la cohérence des données à tout moment.

---

## 2. Choix technologiques

### 2.1 Spring Boot

Spring Boot a été choisi comme socle backend pour plusieurs raisons. D'abord, il permet de développer des web services REST sans écrire de servlet manuellement, conformément aux contraintes du cahier des charges. Son système d'auto-configuration réduit considérablement la configuration. Enfin, l'écosystème Spring couvre l'intégralité des besoins du projet avec une cohérence forte entre les composants.

Le packaging en WAR a été retenu pour permettre un déploiement sur Tomcat externe en production, tout en conservant la possibilité de lancer l'application avec le Tomcat embarqué en développement.

### 2.2 Spring Data JPA / Hibernate

Spring Data JPA a été préféré à JDBC bas niveau car il permet de définir les requêtes courantes via des interfaces sans écrire de code SQL, tout en restant compatible avec des requêtes JPQL personnalisées pour les cas complexes (détection de chevauchement de créneaux, statistiques). Hibernate gère le mapping objet-relationnel et la gestion des transactions.

### 2.3 MySQL

MySQL est la base de données imposée par l'environnement fourni.

### 2.4 iText

iText est la bibliothèque de référence Java pour la génération de PDF.

### 2.5 SpringDoc OpenAPI

SpringDoc génère automatiquement la documentation OpenAPI à partir des annotations. La Swagger UI interactive est disponible sans configuration supplémentaire. Cette approche garantit que la documentation reste toujours synchronisée avec le code.

### 2.6 Lombok

Lombok est utilisé pour éliminer le code répétitif Java : getters, setters, constructeurs. Cela rend les classes de service plus lisibles en se concentrant sur la logique métier. Lombok est exclu du WAR final car il n'est utile qu'à la compilation.

### 2.7 Frontend : HTML + Bootstrap + JS

Le frontend est une Single Page Application (SPA) légère sans framework JavaScript. Ce choix répond à l'exigence d'utiliser HTML/JavaScript, tout en évitant la complexité d'un framework frontend disproportionné par rapport à la taille du projet.

---

## 3. Architecture de l'application

### 3.1 Architecture en couches

L'application suit une architecture en couches strictement séparées, conforme aux bonnes pratiques Spring :

```
┌──────────────────────────────────────────────────────┐
│                  Frontend (SPA)                      │
│             HTML + Bootstrap + JS                    │
└──────────────────────┬───────────────────────────────┘
                       │ HTTP / JSON
┌──────────────────────▼───────────────────────────────┐
│              Couche Controller (REST)                │
│   SeanceController  PhotographeController            │
│   ClientController  StatistiqueController            │
└──────────────────────┬───────────────────────────────┘
                       │ DTOs
┌──────────────────────▼───────────────────────────────┐
│               Couche Service (Métier)                │
│   SeanceService  PhotographeService  ClientService   │
│   FactureService  StatistiqueService                 │
└──────────────────────┬───────────────────────────────┘
                       │ Entités JPA
┌──────────────────────▼───────────────────────────────┐
│              Couche Repository (Données)             │
│   SeanceRepository  PhotographeRepository            │
│   ClientRepository                                   │
└──────────────────────┬───────────────────────────────┘
                       │ JDBC
┌──────────────────────▼───────────────────────────────┐
│                   MySQL 8                            │
└──────────────────────────────────────────────────────┘
```

### 3.2 Séparation Request / Response (DTO)

Le projet distingue systématiquement les objets entrants `Request` des objets sortants `Response`. Les entités JPA ne sont jamais exposées directement en JSON.

### 3.3 Gestion centralisée des erreurs

Un `GlobalExceptionHandler` intercepte toutes les exceptions et les transforme en réponses JSON normalisées avec le code HTTP approprié :

| Exception                         | Code HTTP | Usage                                              |
|-----------------------------------|-----------|----------------------------------------------------|
| `ResourceNotFoundException`       | 404       | Entité inexistante                                 |
| `BusinessException`               | 400       | Violation d'une règle métier                       |
| `ConflictException`               | 409       | Conflit de données (email dupliqué, chevauchement) |
| `MethodArgumentNotValidException` | 400       | Echec de validation Bean                           |
| `Exception` (générique)           | 500       | Erreur inattendue                                  |

Cette centralisation garantit un format d'erreur homogène sur l'ensemble de l'API, facilitant la gestion des erreurs côté frontend.

---

## 4. Modèle de données

### 4.1 Diagramme entité-relation

```
┌──────────────────────┐         ┌────────────────────────────┐
│      photographe     │         │   photographe_specialite   │
├──────────────────────┤         ├────────────────────────────┤
│ id        BIGINT  PK │◄────────│ photographe_id  BIGINT  FK │
│ nom       VARCHAR    │  1   N  │ specialite      ENUM       │
│ email     VARCHAR UQ │         └────────────────────────────┘
│ statut    ENUM       │
└──────────┬───────────┘
           │ 1
           │
           │ N
┌──────────▼───────────┐         ┌──────────────────────────┐
│        seance        │         │           client         │
├──────────────────────┤         ├──────────────────────────┤
│ id             PK    │ N   1   │ id              PK       │
│ date_seance          │────────►│ nom                      │
│ heure_debut          │         │ prenom                   │
│ duree_minutes        │         │ raison_sociale           │
│ lieu                 │         │ adresse                  │
│ type_seance   ENUM   │         │ email                    │
│ statut        ENUM   │         └──────────────────────────┘
│ prix          DECIMAL│
│ photographe_id  FK   │
│ client_id       FK   │
└──────────────────────┘
```

### 4.2 Entités et contraintes

**`photographe`**
- `email` est soumis à une contrainte `UNIQUE` au niveau base de données, doublée d'une vérification applicative pour retourner un message d'erreur explicite.
- `statut` est un ENUM MySQL avec deux valeurs : `ACTIF` / `INACTIF`.

**`photographe_specialite`**
- Table de jonction pour la relation 1-N entre un photographe et ses spécialités.
- La clé primaire composite `(photographe_id, specialite)` empêche les doublons.
- `ON DELETE CASCADE` : la suppression d'un photographe supprime automatiquement ses spécialités.

**`client`**
- Les champs `prenom` et `raison_sociale` sont nullable pour couvrir les deux cas d'usage (particulier vs entreprise).

**`seance`**
- `duree_minutes` est stocké en entier pour faciliter les calculs de chevauchement en SQL.
- `heure_fin` est calculée à la demande (`getHeureFin()` dans l'entité Java) plutôt que stockée, évitant toute incohérence.
- Trois index couvrent les requêtes fréquentes : `(photographe_id, date_seance)`, `statut`, et `client_id`.

### 4.3 Enumerations

| Enum                | Valeurs                                          |
|---------------------|--------------------------------------------------|
| `StatutPhotographe` | `ACTIF`, `INACTIF`                               |
| `StatutSeance`      | `PLANIFIEE`, `CONFIRMEE`, `TERMINEE`, `ANNULEE`  |
| `TypeSeance`        | `REPORTAGE`, `MARIAGE_ET_FETES`, `FAMILLE`       |
| `Specialite`        | `REPORTAGE`, `MARIAGE_ET_FETES`, `FAMILLE`       |

---

## 5. API REST

### 5.1 Respect des principes REST

| Principe                | Implémentation                                                                 |
|-------------------------|--------------------------------------------------------------------------------|
| Méthodes HTTP sémantiques | `GET` lecture, `POST` création, `PUT` mise à jour complète, `PATCH` changement partiel (statut), `DELETE` suppression |
| Codes de réponse cohérents | `200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| Ressources clairement identifiées | URLs : `/api/seances`, `/api/photographes`, `/api/clients` |
| Gestion des erreurs     | Corps JSON normalisé avec `message` pour chaque erreur, géré par `GlobalExceptionHandler` |
| Stateless               | Aucune session serveur, chaque requête est auto-suffisante                      |

### 5.2 Tableau des endpoints

**Séances** — `/api/seances`

| Méthode | URL                           | Description                        | Code succès |
|---------|-------------------------------|------------------------------------|-------------|
| GET     | `/api/seances`                | Liste (filtres optionnels : statut, photographeId, clientId) | 200 |
| GET     | `/api/seances/{id}`           | Détail d'une séance                | 200         |
| POST    | `/api/seances`                | Créer une séance                   | 201         |
| PUT     | `/api/seances/{id}`           | Modifier une séance                | 200         |
| DELETE  | `/api/seances/{id}`           | Supprimer une séance               | 204         |
| PATCH   | `/api/seances/{id}/statut`    | Changer le statut                  | 200         |
| GET     | `/api/seances/{id}/facture`   | Télécharger la facture PDF         | 200         |

**Photographes** — `/api/photographes`

| Méthode | URL                                    | Description                        | Code succès |
|---------|----------------------------------------|------------------------------------|-------------|
| GET     | `/api/photographes`                    | Liste tous les photographes        | 200         |
| GET     | `/api/photographes/{id}`               | Détail d'un photographe            | 200         |
| POST    | `/api/photographes`                    | Créer un photographe               | 201         |
| PUT     | `/api/photographes/{id}`               | Modifier un photographe            | 200         |
| DELETE  | `/api/photographes/{id}`               | Supprimer un photographe           | 204         |
| PUT     | `/api/photographes/{id}/statut`        | Changer le statut (activer/désactiver) | 200    |

**Clients** — `/api/clients`

| Méthode | URL                  | Description               | Code succès |
|---------|----------------------|---------------------------|-------------|
| GET     | `/api/clients`       | Liste tous les clients    | 200         |
| GET     | `/api/clients/{id}`  | Détail d'un client        | 200         |
| POST    | `/api/clients`       | Créer un client           | 201         |
| PUT     | `/api/clients/{id}`  | Modifier un client        | 200         |
| DELETE  | `/api/clients/{id}`  | Supprimer un client       | 204         |

**Statistiques** — `/api/statistiques`

| Méthode | URL                                              | Description                            |
|---------|--------------------------------------------------|----------------------------------------|
| GET     | `/api/statistiques/top-photographes-nombre`      | Top 5 par nombre de séances réalisées  |
| GET     | `/api/statistiques/top-photographes-duree`       | Top 5 par durée cumulée réalisée       |

### 5.3 Documentation Swagger / OpenAPI

La documentation interactive est générée automatiquement par SpringDoc et accessible à :

| URL                                              | Description          |
|--------------------------------------------------|----------------------|
| `http://localhost:8080/sigma-sahur-studio-3-pro/swagger-ui.html`    | Interface Swagger UI |
| `http://localhost:8080/sigma-sahur-studio-3-pro/api-docs`           | Spécification JSON   |

---

## 6. Tests unitaires

### 6.1 Stratégie de test

Les tests portent sur la couche service. Le framework utilisé est JUnit avec Mockito pour l'isolation de la base de données. Les tests sont purement unitaires : aucune base de données n'est sollicitée, ce qui garantit leur rapidité et leur reproductibilité.

### 6.2 Cas de test — SeanceService

| Scénario testé                                        | Résultat attendu            |
|-------------------------------------------------------|-----------------------------|
| Créer une séance à une date passée                    | `BusinessException`         |
| Créer une séance avec un photographe inactif          | `BusinessException`         |
| Créer une séance avec chevauchement détecté           | `ConflictException`         |
| Transition `PLANIFIEE → CONFIRMEE`                    | Succès                      |
| Transition `TERMINEE → PLANIFIEE` (illégale)          | `BusinessException`         |
| Terminer une séance à date future                     | `BusinessException`         |
| Terminer une séance à date passée                     | Succès                      |
| Modifier une séance `ANNULEE`                         | `BusinessException`         |

### 6.3 Cas de test — PhotographeService

| Scénario testé                                              | Résultat attendu               |
|-------------------------------------------------------------|--------------------------------|
| Créer un photographe avec un email déjà utilisé             | `ConflictException`            |
| Désactiver sans séances futures (force=false)               | Désactivation immédiate        |
| Désactiver avec séances futures (force=false)               | `requiresConfirmation=true`    |
| Désactiver avec séances futures (force=true)                | Séances annulées + désactivation |
| Désactiver un photographe déjà inactif                      | `BusinessException`            |

### 6.4 Exécution

```bash
mvn test
```

---

## 7. Livrables et procédure d'installation

### 7.1 Liste des livrables

| Livrable                        | Emplacement                                                       |
|---------------------------------|-------------------------------------------------------------------|
| Application WAR                 | `target/sigma-sahur-studio-3-pro.war`                             |
| Code source                     | Dépôt Git partagé                                                 |
| Script de création du schéma    | `sql/01_schema.sql`                                               |
| Données de démonstration        | `sql/02_data_sample.sql`                                          |
| Documentation API (Swagger UI)  | `http://localhost:8080/sigma-sahur-studio-3-pro/swagger-ui.html`  |
| Procédure d'installation        | `README.md`                                                       |
| Rapport technique               | `rapport_technique.md` (ce document)                              |
| Javadoc                         | `target/javadoc/index.html` (généré par Maven)                    |

### 7.2 Prérequis

- Java 17 ou supérieur
- Maven 3.8 ou supérieur
- Accès réseau à l'instance MySQL `vps817240.ovh.net:3306`

### 7.3 Procédure d'installation

**Étape 1 — Initialiser la base de données** (à faire une seule fois)

```bash
mysql -h vps817240.ovh.net -u info_team02 -p info_team02_schema < sql/01_schema.sql
```

Pour charger des données de démonstration :

```bash
mysql -h vps817240.ovh.net -u info_team02 -p info_team02_schema < sql/02_data_sample.sql
```

**Étape 2 — Compiler l'application**

```bash
mvn package -DskipTests
```

**Étape 3 — Démarrer l'application**

Option A — Tomcat embarqué (développement) :

```bash
java -jar target/sigma-sahur-studio-3-pro.war
```

Option B — Tomcat externe (production) :

```bash
cp target/sigma-sahur-studio-3-pro.war $TOMCAT_HOME/webapps/sigma.war
```

**Étape 4 — Accéder à l'application**

| URL                                                                 | Description          |
|---------------------------------------------------------------------|----------------------|
| `http://localhost:8080/sigma-sahur-studio-3-pro/`                   | Application          |
| `http://localhost:8080/sigma-sahur-studio-3-pro/swagger-ui.html`    | Documentation API    |

---

## 8. Conclusion

L'application Sigma Sahur Studio 3 Pro implémente l'intégralité des fonctionnalités définies dans le cahier des charges. L'architecture en couches de Spring Boot offre une séparation claire des responsabilités, facilitant la maintenance et l'évolution du code. Les règles métier critiques (machine à états des séances, détection de chevauchement, workflow de désactivation en deux étapes) sont encapsulées dans la couche service et validées par des tests unitaires reproductibles.