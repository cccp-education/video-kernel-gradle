# youtube-auth-plugin

Plugin Gradle N1 fournissant la tâche **`obtainYouTubeRefreshToken`** : il exécute
une fois le flux **OAuth 2.0 loopback** de Google et écrit le **refresh token
YouTube** dans un fichier de credentials gitignoré (`youtube.yaml`).

Il complète le port N1 `video.kernel.publish` : on obtient le token ici, puis on
publie avec `publishHyperframesToYouTube` / `publishCapsuleToYouTube`.

## Pourquoi un plugin séparé

La lib `video-kernel` est **pure** (aucune dépendance Gradle). Une tâche Gradle
exige `org.gradle.api.*` → elle vit dans ce module **frère**, le noyau restant
intact.

## Utilisation

```kotlin
// build.gradle.kts du consommateur
plugins {
    id("education.cccp.video.auth") version "0.0.1"
}
youtubeAuth {
    credentialsFile.set("youtube.yaml") // gitignore, porte clientId + clientSecret
    port.set(8765)                      // URI de redirection a enregistrer : http://localhost:8765/
}
```

### Session navigateur isolée (pas de cookie du profil par défaut)

Par défaut la tâche **n'ouvre pas le navigateur par défaut** : elle lance
Chrome/Chromium sur un `--user-data-dir` **dédié et neuf** à chaque exécution.
Google ne voit donc aucun cookie du profil habituel et affiche le sélecteur de
compte — indispensable quand le profil par défaut est déjà connecté à un autre
compte Google que celui de la chaîne cible.

```kotlin
youtubeAuth {
    // Facultatif : binaire explicite (sinon auto-détection Chrome/Chromium/Brave/Edge/Vivaldi).
    browserExecutable.set("/usr/bin/google-chrome")
    // Facultatif : base du profil isolé (défaut <buildDir>/youtube-auth/browser-profile).
    browserProfileDir.set("build/youtube-auth/profile")
    // false = réutiliser le profil isolé (compte déjà connecté conservé).
    freshBrowserProfile.set(true)
}
```

Le profil neuf est supprimé après succès (cookies et jetons de session ne
traînent pas). Si aucun Chromium n'est installé, la tâche retombe sur le
navigateur par défaut et **avertit** que la session courante peut appartenir à
un autre compte.

Prérequis (une fois) :
1. Client OAuth **Web** avec l'URI de redirection autorisée `http://localhost:8765/`.
2. `youtube.yaml` gitignoré contenant au moins `clientId` et `clientSecret`.

Puis :

```bash
./gradlew obtainYouTubeRefreshToken
```

La tâche affiche l'URL de consentement (ouverte dans le navigateur), attend la
redirection locale, échange le code contre les jetons et réécrit la ligne
`refreshToken:` — toutes les autres lignes du fichier sont préservées.

## Tâches

| Tâche | Groupe | Rôle |
|---|---|---|
| `obtainYouTubeRefreshToken` | `deploy` | Obtenir un refresh token OAuth 2.0 et l'écrire dans le fichier de credentials |

## Tests

- Unit : `YouTubeConsentUrl`, `YouTubeTokenParser`, `CredentialsFileRewriter`,
  loopback roundtrip.
- BDD : `youtube_auth.feature` (`cucumberTest`).
- Fonctionnel : `YouTubeAuthFunctionalTest` (Gradle TestKit).

`./gradlew build` (le check exécute unit + cucumber ; le CI ajoute functional).

## Sécurité

Les secrets (`clientSecret`, `refreshToken`) ne sont **jamais** journalisés et
vivent uniquement dans un fichier gitignoré.
