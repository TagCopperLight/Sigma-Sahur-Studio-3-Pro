# Sigma-Sahur-Studio-3-Pro
Système de gestion d'un studio de photographie professionnel.

Groupe 2 : Nicolas DEVOS, Malo JOANNON, Lila HAMDI, Pierre-Louis ROMAND, Julien BOURDET

## Prérequis

- Java 17+
- Maven 3.8+
- Accès à l'instance MySQL
- `mysql` ou `mycli` pour interagir avec la base de données

## 1. Initialiser la base de données

Appliquer le schéma (à faire une seule fois, ou pour réinitialiser) :

```bash
# mysql
mysql -h vps817240.ovh.net -u info_team02 -p info_team02_schema < sql/01_schema.sql

# mycli
mycli -h vps817240.ovh.net -u info_team02 -p info_team02_schema < sql/01_schema.sql
```

Des données de démonstration sont disponibles optionnellement :

```bash
# mysql
mysql -h vps817240.ovh.net -u info_team02 -p info_team02_schema < sql/02_data_sample.sql

# mycli
mycli -h vps817240.ovh.net -u info_team02 -p info_team02_schema < sql/02_data_sample.sql
```

Se connecter en session interactive :

```bash
# mysql
mysql -h vps817240.ovh.net -u info_team02 -p info_team02_schema

# mycli
mycli -h vps817240.ovh.net -u info_team02 -p info_team02_schema
```

## 2. Configurer les accès à la base de données

L'application charge les credentials depuis un fichier `application-local.properties` qui n'est **pas versionné** (gitignored).

Copier le fichier d'exemple et le remplir :

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

Éditer `src/main/resources/application-local.properties` :

```properties
spring.datasource.url=jdbc:mysql://vps817240.ovh.net:3306/info_team02_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris
spring.datasource.username=info_team02
spring.datasource.password=<mot_de_passe>
```

> Sans ce fichier, l'application démarre mais échoue à se connecter à la base de données.

## 3. Compiler

Sans lancer les tests :

```bash
mvn package -DskipTests
```

Avec les tests :

```bash
mvn package
```

## 4. Lancer les tests

```bash
mvn test
```

Les rapports sont générés dans `target/surefire-reports/`.

## 5. Démarrer l'application

### Option A — Tomcat embarqué (développement)

```bash
java -jar target/sigma-sahur-studio-3-pro.war
```

### Option B — Tomcat externe (production)

Copier le WAR dans le répertoire webapps de Tomcat :

```bash
cp target/sigma-sahur-studio-3-pro.war $TOMCAT_HOME/webapps/sigma.war
```

## 6. Générer la Javadoc

```bash
mvn javadoc:javadoc
```

La documentation HTML est générée dans `target/javadoc/`. Ouvrir `target/javadoc/index.html` dans un navigateur pour la consulter.

Pour inclure la génération lors du packaging :

```bash
mvn package -DskipTests
```

La Javadoc est alors produite automatiquement avec le WAR.

## 7. Accès

| URL | Description |
|-----|-------------|
| http://localhost:8080/sigma-sahur-studio-3-pro/ | Application |
| http://localhost:8080/sigma-sahur-studio-3-pro/swagger-ui.html | Swagger UI |
| http://localhost:8080/sigma-sahur-studio-3-pro/api-docs | OpenAPI JSON |