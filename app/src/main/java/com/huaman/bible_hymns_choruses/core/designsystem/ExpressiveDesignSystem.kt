package com.huaman.bible_hymns_choruses.core.designsystem

import androidx.compose.runtime.Composable
import com.huaman.bible_hymns_choruses.core.datastore.ThemeMode
import com.huaman.bible_hymns_choruses.ui.theme.BiblehymnschorusesTheme
import androidx.compose.foundation.isSystemInDarkTheme

/**
 * Wrapper connecting [ThemeMode] from DataStore to the existing Material 3 Expressive
 * [BiblehymnschorusesTheme] (`MotionScheme.expressive()` + `Google Sans Flex` 30-style scale).
 */
@Composable
fun ExpressiveAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    BiblehymnschorusesTheme(
        darkTheme = useDarkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
