package com.huaman.bible_hymns_choruses.data.mapper

import com.huaman.bible_hymns_choruses.data.local.entity.BookEntity
import com.huaman.bible_hymns_choruses.data.local.entity.BookmarkEntity
import com.huaman.bible_hymns_choruses.data.local.entity.SongEntity
import com.huaman.bible_hymns_choruses.data.local.entity.SongSummaryProjection
import com.huaman.bible_hymns_choruses.data.local.entity.VerseEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseWithBookProjection
import com.huaman.bible_hymns_choruses.domain.model.BibleBook
import com.huaman.bible_hymns_choruses.domain.model.BibleVerse
import com.huaman.bible_hymns_choruses.domain.model.BookmarkItem
import com.huaman.bible_hymns_choruses.domain.model.BookmarkType
import com.huaman.bible_hymns_choruses.domain.model.SongDetail
import com.huaman.bible_hymns_choruses.domain.model.SongStanzaBlock
import com.huaman.bible_hymns_choruses.domain.model.SongSummary
import com.huaman.bible_hymns_choruses.domain.model.SongType
import com.huaman.bible_hymns_choruses.domain.model.Testament
import com.huaman.bible_hymns_choruses.domain.model.VerseSearchMatch

fun BookEntity.toDomain(): BibleBook = BibleBook(
    id = id,
    testament = if (testament.equals("AT", ignoreCase = true)) Testament.AT else Testament.NT,
    name = name,
    abbreviation = abbreviation,
    chapterCount = chapterCount
)

fun VerseEntity.toDomain(): BibleVerse = BibleVerse(
    id = id,
    bookId = bookId,
    chapter = chapter,
    verse = verse,
    text = text
)

fun VerseWithBookProjection.toDomain(): VerseSearchMatch = VerseSearchMatch(
    id = id,
    bookId = bookId,
    bookName = bookName,
    bookAbbr = bookAbbr,
    chapter = chapter,
    verse = verse,
    text = text
)

fun SongSummaryProjection.toDomain(): SongSummary = SongSummary(
    id = id,
    type = SongType.fromDb(type),
    number = number,
    title = title,
    subtitleEn = subtitleEn,
    tonality = tonality,
    category = category
)

fun SongEntity.toSummaryDomain(): SongSummary = SongSummary(
    id = id,
    type = SongType.fromDb(type),
    number = number,
    title = title,
    subtitleEn = subtitleEn,
    tonality = tonality,
    category = category
)

fun SongEntity.toDetailDomain(): SongDetail {
    val songType = SongType.fromDb(type)
    val parsedBlocks = parseSongBlocks(lyrics, songType)
    return SongDetail(
        id = id,
        type = songType,
        number = number,
        title = title,
        subtitleEn = subtitleEn,
        tonality = tonality,
        category = category,
        rawLyrics = lyrics,
        blocks = parsedBlocks
    )
}

fun BookmarkEntity.toDomain(): BookmarkItem = BookmarkItem(
    id = id,
    itemType = BookmarkType.fromDb(itemType),
    referenceId = referenceId,
    titlePreview = titlePreview,
    contentPreview = contentPreview,
    colorHex = colorHex,
    note = note,
    createdAt = createdAt
)

fun BookmarkItem.toEntity(): BookmarkEntity = BookmarkEntity(
    id = id,
    itemType = itemType.dbValue,
    referenceId = referenceId,
    titlePreview = titlePreview,
    contentPreview = contentPreview,
    colorHex = colorHex,
    note = note,
    createdAt = createdAt
)

private val STANZA_HEADER_REGEX = Regex("""^(CORO|\d{1,2})$""", RegexOption.IGNORE_CASE)

private fun parseSongBlocks(rawLyrics: String, type: SongType): List<SongStanzaBlock> {
    if (rawLyrics.isBlank()) return emptyList()
    if (type == SongType.CHORUS) {
        val lines = rawLyrics.lines().map { it.trim() }.filter { it.isNotEmpty() }
        return listOf(SongStanzaBlock(label = "CORO", isChorus = true, lines = lines))
    }

    val rawParagraphs = rawLyrics.split(Regex("""\n\s*\n"""))
    return rawParagraphs.mapNotNull { paragraph ->
        val lines = paragraph.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return@mapNotNull null
        val firstLine = lines.first()
        if (STANZA_HEADER_REGEX.matches(firstLine) && lines.size > 1) {
            val isChorus = firstLine.equals("CORO", ignoreCase = true)
            SongStanzaBlock(
                label = if (isChorus) "CORO" else firstLine,
                isChorus = isChorus,
                lines = lines.drop(1)
            )
        } else {
            SongStanzaBlock(
                label = "",
                isChorus = false,
                lines = lines
            )
        }
    }
}
