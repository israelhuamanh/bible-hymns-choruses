package com.huaman.bible_hymns_choruses.core.datastore

import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Offline-first user preferences model persisted via Jetpack DataStore:
 * - Last read Bible book ID and chapter number
 * - Reader & lyric font size (sp)
 * - Google Sans Flex variable axes: font weight (`wght`: 400..700) and grade (`GRAD`: -50..150)
 * - Theme preference (SYSTEM, LIGHT, DARK)
 * - Last active navigation tab (BIBLE, SONGS, BOOKMARKS)
 * - First-time onboarding completion state (`hasCompletedOnboarding`)
 */
@Immutable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Immutable
enum class ActiveAppTab {
    BIBLE,
    SONGS,
    BOOKMARKS,
    NOTES
}

@Immutable
data class UserPreferences(
    val selectedBibleVersionId: String = DEFAULT_BIBLE_VERSION_ID,
    val lastBookId: Int = 1,          // Génesis
    val lastChapter: Int = 1,         // Capítulo 1
    val fontSizeSp: Float = 18f,      // 14f .. 28f
    val fontWeight: Int = 400,        // Google Sans Flex Weight (400 Normal, 500 Medium, 600 SemiBold, 700 Bold)
    val fontGrade: Int = 0,           // Google Sans Flex Grade (-50 .. 150)
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val lastActiveTab: ActiveAppTab = ActiveAppTab.BIBLE,
    val hasCompletedOnboarding: Boolean = false
) {
    companion object {
        const val DEFAULT_BIBLE_VERSION_ID = "RVR1960"
    }
}

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val SELECTED_BIBLE_VERSION_ID = stringPreferencesKey("selected_bible_version_id")
        val LAST_BOOK_ID = intPreferencesKey("last_book_id")
        val LAST_CHAPTER = intPreferencesKey("last_chapter")
        val FONT_SIZE_SP = floatPreferencesKey("font_size_sp")
        val FONT_WEIGHT = intPreferencesKey("font_weight")
        val FONT_GRADE = intPreferencesKey("font_grade")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_ACTIVE_TAB = stringPreferencesKey("last_active_tab")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val themeName = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val tabName = prefs[Keys.LAST_ACTIVE_TAB] ?: ActiveAppTab.BIBLE.name
            val versionId = prefs[Keys.SELECTED_BIBLE_VERSION_ID]
                ?.takeIf { it.isNotBlank() }
                ?: UserPreferences.DEFAULT_BIBLE_VERSION_ID

            UserPreferences(
                selectedBibleVersionId = versionId,
                lastBookId = (prefs[Keys.LAST_BOOK_ID] ?: 1).coerceIn(1, 66),
                lastChapter = (prefs[Keys.LAST_CHAPTER] ?: 1).coerceAtLeast(1),
                fontSizeSp = (prefs[Keys.FONT_SIZE_SP] ?: 18f).coerceIn(14f, 30f),
                fontWeight = (prefs[Keys.FONT_WEIGHT] ?: 400).coerceIn(300, 700),
                fontGrade = (prefs[Keys.FONT_GRADE] ?: 0).coerceIn(-50, 150),
                themeMode = runCatching { ThemeMode.valueOf(themeName) }.getOrDefault(ThemeMode.SYSTEM),
                lastActiveTab = runCatching { ActiveAppTab.valueOf(tabName) }.getOrDefault(ActiveAppTab.BIBLE),
                hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false
            )
        }

    suspend fun completeOnboarding() {
        dataStore.edit { prefs ->
            prefs[Keys.HAS_COMPLETED_ONBOARDING] = true
        }
    }

    suspend fun updateSelectedBibleVersion(versionId: String) {
        dataStore.edit { prefs ->
            prefs[Keys.SELECTED_BIBLE_VERSION_ID] = versionId
        }
    }

    suspend fun updateLastReadingPosition(bookId: Int, chapter: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_BOOK_ID] = bookId.coerceIn(1, 66)
            prefs[Keys.LAST_CHAPTER] = chapter.coerceAtLeast(1)
        }
    }

    suspend fun updateTypographySettings(
        fontSizeSp: Float? = null,
        fontWeight: Int? = null,
        fontGrade: Int? = null
    ) {
        dataStore.edit { prefs ->
            fontSizeSp?.let { prefs[Keys.FONT_SIZE_SP] = it.coerceIn(14f, 30f) }
            fontWeight?.let { prefs[Keys.FONT_WEIGHT] = it.coerceIn(300, 700) }
            fontGrade?.let { prefs[Keys.FONT_GRADE] = it.coerceIn(-50, 150) }
        }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode.name
        }
    }

    suspend fun updateLastActiveTab(tab: ActiveAppTab) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_ACTIVE_TAB] = tab.name
        }
    }
}
