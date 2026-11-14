package com.huaman.bible_hymns_choruses.data.repository

import com.huaman.bible_hymns_choruses.data.local.entity.BookEntity
import java.text.Normalizer

/**
 * Result of classifying a user query before touching SQLite/FTS4.
 */
sealed interface QueryRoute {
    data object Empty : QueryRoute
    data class PureNumber(val number: Int) : QueryRoute
    data class BibleReference(
        val book: BookEntity,
        val chapter: Int,
        val startVerse: Int? = null,
        val endVerse: Int? = null
    ) : QueryRoute

    data class FullText(
        val normalizedQuery: String,
        val ftsMatchExpression: String
    ) : QueryRoute
}

/**
 * Smart Query Router & Normalizer:
 * 1. Pure number (e.g. "104") -> Direct indexed lookup (`idx_songs_type_number`) in < 1 ms.
 * 2. Bible reference (e.g. "Juan 3:16", "Sal 23", "1 Cor 13:4-7") -> Resolved via Regex against
 *    in-memory cached books (`0 ms`) without hitting FTS4.
 * 3. Free text -> Strips accents, sanitizes FTS4 operators, and appends prefix wildcards (`palabra*`).
 */
object SmartQueryRouter {

    private val PURE_NUMBER_REGEX = Regex("""^\s*#?(\d{1,4})\s*$""")

    // Matches: "1 Juan 3:16", "Juan 3:16-18", "Sal 23", "Salmos 23 1", "Apoc 21:4"
    private val BIBLE_REF_REGEX = Regex(
        pattern = """^\s*([1-3]?\s*[a-zA-ZáéíóúÁÉÍÓÚñÑ\.]+(?:\s+[a-zA-ZáéíóúÁÉÍÓÚñÑ]+)?)\s+(\d{1,3})(?:\s*[:\.\s]\s*(\d{1,3})(?:\s*[-–]\s*(\d{1,3}))?)?\s*$"""
    )

    private val NON_ALPHANUM_REGEX = Regex("""[^\p{L}\p{N}\s]""")
    private val MULTI_SPACE_REGEX = Regex("""\s+""")
    private val COMBINING_MARKS_REGEX = Regex("""\p{Mn}+""")

    fun normalizeText(input: String): String {
        if (input.isBlank()) return ""
        val decomposed = Normalizer.normalize(input.lowercase(), Normalizer.Form.NFD)
        val stripped = COMBINING_MARKS_REGEX.replace(decomposed, "")
        val cleaned = NON_ALPHANUM_REGEX.replace(stripped, " ")
        return MULTI_SPACE_REGEX.replace(cleaned, " ").trim()
    }

    fun buildFtsPrefixQuery(rawQuery: String): String {
        val normalized = normalizeText(rawQuery)
        if (normalized.isBlank()) return ""
        val tokens = normalized.split(" ").filter { it.isNotBlank() }
        if (tokens.isEmpty()) return ""
        return tokens.joinToString(separator = " ") { token -> "${token}*" }
    }

    fun route(rawQuery: String, books: List<BookEntity>): QueryRoute {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return QueryRoute.Empty

        // 1. Pure number check ("104", "#104")
        PURE_NUMBER_REGEX.matchEntire(trimmed)?.let { match ->
            val num = match.groupValues[1].toIntOrNull()
            if (num != null && num > 0) {
                return QueryRoute.PureNumber(num)
            }
        }

        // 2. Bible reference check ("Juan 3:16", "Sal 23", "1 Cor 13:4-7")
        if (books.isNotEmpty()) {
            BIBLE_REF_REGEX.matchEntire(trimmed)?.let { match ->
                val rawBookPart = normalizeText(match.groupValues[1])
                val chapter = match.groupValues[2].toIntOrNull()
                val startVerse = match.groupValues[3].toIntOrNull()
                val endVerse = match.groupValues[4].toIntOrNull() ?: startVerse

                if (rawBookPart.isNotBlank() && chapter != null && chapter >= 1) {
                    val matchedBook = findMatchingBook(rawBookPart, books)
                    if (matchedBook != null && chapter <= matchedBook.chapterCount) {
                        return QueryRoute.BibleReference(
                            book = matchedBook,
                            chapter = chapter,
                            startVerse = startVerse,
                            endVerse = endVerse
                        )
                    }
                }
            }
        }

        // 3. Free-text FTS4 prefix query
        val normalized = normalizeText(trimmed)
        val ftsExpr = buildFtsPrefixQuery(trimmed)
        if (ftsExpr.isBlank()) return QueryRoute.Empty
        return QueryRoute.FullText(
            normalizedQuery = normalized,
            ftsMatchExpression = ftsExpr
        )
    }

    private fun findMatchingBook(normalizedBookQuery: String, books: List<BookEntity>): BookEntity? {
        // Exact match on normalized name or abbreviation first
        books.firstOrNull { book ->
            normalizeText(book.name) == normalizedBookQuery ||
                normalizeText(book.abbreviation) == normalizedBookQuery
        }?.let { return it }

        // Prefix match on book name, abbreviation, or search_name tokens
        return books.firstOrNull { book ->
            val normName = normalizeText(book.name)
            val normAbbr = normalizeText(book.abbreviation)
            normName.startsWith(normalizedBookQuery) ||
                normAbbr.startsWith(normalizedBookQuery) ||
                book.searchName.split(" ").any { it.startsWith(normalizedBookQuery) }
        }
    }
}
