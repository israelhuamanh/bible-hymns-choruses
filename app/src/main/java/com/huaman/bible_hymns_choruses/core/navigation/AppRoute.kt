package com.huaman.bible_hymns_choruses.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation Compose routes (`@Serializable`) for offline navigation across:
 * - Bible Reader (`BibleReaderRoute`)
 * - Hymns & Choruses Catalog (`SongsRoute`)
 * - Song Detail (`SongDetailRoute`)
 * - Bookmarks / Saved Items (`BookmarksRoute`)
 * - Universal Search (`GlobalSearchRoute`)
 */
sealed interface AppRoute {

    @Serializable
    data class BibleReaderRoute(
        val bookId: Int = 1,
        val chapter: Int = 1,
        val highlightVerse: Int? = null
    ) : AppRoute

    @Serializable
    data class SongsRoute(
        val initialType: String = "HYMN"
    ) : AppRoute

    @Serializable
    data class SongDetailRoute(
        val songId: Int
    ) : AppRoute

    @Serializable
    data object BookmarksRoute : AppRoute

    @Serializable
    data class GlobalSearchRoute(
        val initialQuery: String = ""
    ) : AppRoute
}
