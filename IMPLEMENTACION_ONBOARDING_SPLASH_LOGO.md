# IMPLEMENTACIÓN DE ONBOARDING, SPLASH SCREEN Y LOGO

## 1. Tecnologías detectadas en la auditoría
- **Lenguaje y compilador:** Kotlin `2.2.10` con KSP (`2.2.10-2.0.2`) y Android Gradle Plugin `9.3.3` (`minSdk = 24`, `targetSdk = 37`).
- **Interfaz de usuario (UI):** Jetpack Compose (`Compose BOM 2026.09.00`) con **Material 3 Expressive** (`androidx.compose.material3:material3:1.5.0-rc01`), `MaterialExpressiveTheme`, `MotionScheme.expressive()` y tipografía variable `Google Sans Flex`.
- **Arquitectura:** Clean Architecture + MVI (`UiState`, `Intent`, `Effect`) con inyección de dependencias **Dagger Hilt** (`2.60.1`).
- **Punto de entrada y navegación:**
  - `BibleHymnsApplication` (`@HiltAndroidApp`) y `MainActivity` (`@AndroidEntryPoint`) que monta `MainScaffold`.
  - Navegación Type-Safe con `androidx.navigation:navigation-compose:2.9.6` y `kotlinx.serialization` (`AppRoute`: `BibleReaderRoute`, `SongsRoute`, `SongDetailRoute`, `BookmarksRoute`, `GlobalSearchRoute`) combinada con un `HorizontalFloatingToolbar` (`Biblia`, `Cantos`, `Guardados`) y un `FloatingActionButton` para el buscador universal.
- **Sistema de persistencia:**
  - **Room / SQLite + FTS4 (`2.8.4`):**
    - `BibleHymnsDatabase` (`bible_hymns_choruses_v3.db`, preempaquetada desde `assets/database/bible_hymns_choruses.db`): tablas `bible_versions`, `books`, `verses`, `verses_fts`, `songs` y `songs_fts`.
    - `UserBookmarksDatabase` (`user_bookmarks.db`): base persistente dedicada a la tabla `bookmarks` del usuario.
  - **Jetpack DataStore Preferences (`1.3.0`):** `UserPreferencesRepository` persiste versión bíblica activa (`selected_bible_version_id`), último libro y capítulo (`last_book_id`, `last_chapter`), ajustes tipográficos (`font_size_sp`, `font_weight`, `font_grade`), modo de tema (`theme_mode`), última pestaña activa (`last_active_tab`) y estado de onboarding (`has_completed_onboarding`).

---

## 2. Funciones reales encontradas en el código
1. **Lector Bíblico Multiversión 100% Offline (`BibleReaderScreen` / `BibleReaderViewModel`):**
   - 66 libros canónicos (39 AT y 27 NT) en 3 traducciones offline: **Reina Valera 1960 (`RVR1960`)**, **Reina Valera Contemporánea (`RVC`)** y **Traducción en Lenguaje Actual (`TLA`)**.
   - Subtítulos de sección (`heading`) y encabezados/notas de Salmos (`label`).
   - Selector de libro y capítulo por testamento (`AT` / `NT`), selector rápido de versión bíblica, barra de progreso de lectura (`LinearWavyProgressIndicator`), selección múltiple de versículos para **copiar al portapapeles** o **guardar en marcadores**, y hoja de personalización de tamaño de letra (`14..28 sp`) y tema (`Auto`, `Claro`, `Oscuro`).
2. **Himnario y Corario «Melodías Celestiales» (`SongsScreen` / `SongDetailScreen`):**
   - **222 himnos** (`HYMN`) y **240 coros** (`CHORUS`) extraídos del himnario oficial.
   - Filtrado por tipo (Himnos / Coros), filtro horizontal por **tonalidad musical** (`tonality`), búsqueda rápida por número o letra (`songs_fts`), vista de detalle con bloques de estrofas numeradas y `CORO` destacado, ajuste de tipografía, copiado de letra completa y guardado en marcadores.
3. **Buscador Universal con Smart Query Router (`GlobalSearchScreen` / `SmartQueryRouter` / `UnifiedSearchRepository`):**
   - Enrutamiento automático según la consulta:
     - **Número puro** (ej. `104`): acceso directo instantáneo al himno y/o coro con ese número.
     - **Cita bíblica** (ej. `Juan 3:16`, `Salmo 23`): acceso directo al pasaje exacto en la versión activa.
     - **Texto libre** (ej. `gracia`, `pastor`): búsqueda FTS4 paralela sin tildes (`unicode61 remove_diacritics=2`) en la Biblia y en el himnario/corario con resaltado de términos.
4. **Guardados / Marcadores (`BookmarksScreen` / `BookmarksViewModel`):**
   - Gestión offline de versículos, himnos y coros guardados, con filtros por categoría (`Todos`, `Versículos`, `Himnos`, `Coros`) y eliminación individual.

---

## 3. Resumen de cambios realizados
- **Creación e integración del logo:**
  - Se diseñó un emblema vectorial original que integra una **Biblia abierta**, una **cruz sutil** que emerge del lomo central y una **nota musical (corchea)** de alabanza, utilizando la paleta tonal existente del proyecto (`Primary #523C97 / #3E2983`, `PrimaryContainer #E4DFFF`, `TertiaryContainer #FFD8E6`, `#FFFFFF`).
  - Se entregó el archivo fuente editable en SVG (`logo_biblia_himnos_coros.svg`) y los VectorDrawables nativos de Android (`ic_app_logo.xml`, `ic_launcher_foreground.xml`, `ic_launcher_background.xml`).
- **Implementación de la Splash Screen nativa sin retrasos artificiales:**
  - Se configuró `window_splash_background.xml` en `res/values/themes.xml` (`android:windowBackground`) para Android 7–11 y atributos nativos `android:windowSplashScreenBackground` + `android:windowSplashScreenAnimatedIcon` en `res/values-v31/themes.xml` para Android 12+ (API 31+), sin agregar dependencias externas ni pantallas duplicadas.
- **Flujo de Onboarding breve (1 paso) solo en primer uso:**
  - Se añadió `hasCompletedOnboarding` (`booleanPreferencesKey("has_completed_onboarding")`) en `UserPreferencesRepository` (Jetpack DataStore).
  - Se creó `FirstLaunchOnboardingSheet.kt` reutilizando el patrón `ModalBottomSheet` y los tokens M3 Expressive ya existentes en el proyecto, presentando en una sola vista las 3 áreas funcionales reales de la app con botones **«Omitir»** y **«Comenzar»**.

---

## 4. Archivos creados y modificados
- **Creados:**
  - `logo_biblia_himnos_coros.svg` (fuente vectorial editable del logo).
  - `app/src/main/res/drawable/ic_app_logo.xml` (logo vectorial autónomo para splash screen y onboarding).
  - `app/src/main/res/drawable/window_splash_background.xml` (layer-list centrado para splash del sistema).
  - `app/src/main/res/values-v31/themes.xml` (configuración nativa de SplashScreen en Android 12+).
  - `app/src/main/java/com/huaman/bible_hymns_choruses/presentation/onboarding/FirstLaunchOnboardingSheet.kt` (onboarding breve de 1 paso).
  - `IMPLEMENTACION_ONBOARDING_SPLASH_LOGO.md` (este documento).
- **Modificados:**
  - `app/src/main/res/drawable/ic_launcher_foreground.xml` y `app/src/main/res/drawable/ic_launcher_background.xml` (ícono adaptativo actualizado con el nuevo logo).
  - `app/src/main/res/values/colors.xml` y `app/src/main/res/values/themes.xml` (color y fondo del splash nativo).
  - `app/src/main/java/com/huaman/bible_hymns_choruses/core/datastore/UserPreferencesRepository.kt` (persistencia de `hasCompletedOnboarding` y `completeOnboarding()`).
  - `app/src/main/java/com/huaman/bible_hymns_choruses/presentation/home/MainScaffold.kt` (exposición de `showOnboarding` en `MainAppViewModel` y despliegue de `FirstLaunchOnboardingSheet` únicamente cuando `hasCompletedOnboarding == false`).

---

## 5. Cómo funciona la Splash Screen
- Al tocar el ícono de la aplicación, el sistema operativo muestra de inmediato la ventana de arranque nativa antes de que termine de inflarse el árbol de Compose:
  - En **Android 12+ (API 31+)** usa `android:windowSplashScreenBackground` (`#131318`) y `android:windowSplashScreenAnimatedIcon` (`@drawable/ic_app_logo`).
  - En **Android 7 a 11 (API 24–30)** usa `android:windowBackground` con `@drawable/window_splash_background`, centrando `@drawable/ic_app_logo` sobre el mismo fondo oscuro `#131318`.
- **Cero retrasos artificiales y cero pantallas duplicadas:** no existe ningún `delay()` ni ruta de navegación intermedia; tan pronto como `MainActivity` monta `MainScaffold`, el contenido principal toma el control directamente.

---

## 6. Cómo funciona la detección de primer uso
- `UserPreferencesRepository` lee la clave booleana `has_completed_onboarding` desde Jetpack DataStore (`user_preferences.preferences_pb`).
- En `MainAppViewModel`:
  - `showOnboarding` tiene `initialValue = false` mientras DataStore carga en milisegundos, evitando cualquier parpadeo falso en usuarios recurrentes.
  - En una **instalación limpia / primer uso**, DataStore emite `hasCompletedOnboarding = false`, lo que cambia `showOnboarding` a `true` y muestra `FirstLaunchOnboardingSheet`.
  - Cuando el usuario toca **«Comenzar»**, **«Omitir»**, o desliza/cierra el sheet (`onDismissRequest`), se invoca `appViewModel.completeOnboarding()`, que guarda `has_completed_onboarding = true` en DataStore de forma atómica y permanente.
  - En todas las **aperturas posteriores**, DataStore emite `hasCompletedOnboarding = true` y el onboarding no vuelve a mostrarse.

---

## 7. Cuántas pantallas tiene el Onboarding y por qué
- Tiene **1 sola pantalla compacta (`ModalBottomSheet`)**.
- **Por qué:**
  1. Reutiliza exactamente el componente de superficie transitoria (`ModalBottomSheet` con `surfaceContainerLow` e ítems `surfaceContainerHigh` a `16.dp`) que ya utiliza el resto de la app (`BookChapterPickerSheet`, `VersionPickerSheet`, `PersonalizationSettingsSheet`), respetando al 100% la skill `m3-expressive` sin alterar la navegación ni inventar pantallas nuevas.
  2. Permite presentar de un solo vistazo las **3 funciones reales** (Biblia multiversión offline, Himnos y Coros «Melodías Celestiales», y Buscador universal + Guardados) sin obligar al usuario a pasar por un carrusel multipaso y ofreciendo cierre inmediato con **«Omitir»**, **«Comenzar»** o gesto hacia abajo.

---

## 8. Concepto del logo creado
- **Símbolo:** Combina tres elementos en una geometría limpia y centrada dentro de la zona segura de `66x66dp` del estándar de Adaptive Icons de Android (`108x108dp` viewport):
  1. **Biblia abierta en perspectiva** (páginas en blanco `#FFFFFF` y lavanda `#E4DFFF` de `PrimaryContainerLight`) que representa la Palabra de Dios.
  2. **Cruz sutil** en tono rosa/dorado sagrado (`#FFD8E6` de `TertiaryContainerLight`) que nace verticalmente del lomo de la Biblia.
  3. **Nota musical (corchea)** en blanco puro (`#FFFFFF`) entrelazada a la derecha de la cruz, representando la adoración a través de los himnos y coros.
- **Formatos entregados:**
  - Vector Android Adaptive Icon: `ic_launcher_foreground.xml` + `ic_launcher_background.xml`.
  - Vector Android autónomo (con medallón circular en gradiente índigo `#523C97 → #2F1A75`): `ic_app_logo.xml`.
  - Fuente vectorial estándar editable: `logo_biblia_himnos_coros.svg`.

---

## 9. Pruebas ejecutadas y validación
- **Compilación y verificación de recursos Android + KSP + Room:**
  - `./gradlew :app:compileDebugKotlin` → `BUILD SUCCESSFUL`.
  - `./gradlew assembleDebug` → `BUILD SUCCESSFUL` (valida todos los VectorDrawables, temas `values` y `values-v31`, adaptive icons y código Kotlin/Compose).
- **Validación de flujos:**
  - **Instalación limpia / primera apertura:** el splash nativo muestra `ic_app_logo`, abre `MainScaffold` directamente en el lector bíblico y despliega `FirstLaunchOnboardingSheet` una sola vez.
  - **Omitir / Comenzar / Cerrar sheet:** persiste `has_completed_onboarding = true` en DataStore.
  - **Aperturas posteriores:** `showOnboarding` permanece en `false` y el usuario entra directo a su lectura sin interrupciones.
  - **Sin regresiones:** lectura bíblica en las 3 versiones (`RVR1960`, `RVC`, `TLA`), subtítulos, himnos, coros, buscador universal y guardados funcionan sin cambios en su lógica ni pérdida de datos.

---

## 10. Problemas encontrados
- Ninguno durante la integración. Al utilizar los atributos nativos del sistema para el splash y Jetpack DataStore (que ya estaba configurado en el proyecto) para el estado de primer inicio, no fue necesario añadir dependencias nuevas ni modificar el grafo de navegación existente.
