package com.huaman.bible_hymns_choruses.data.mapper

import com.huaman.bible_hymns_choruses.data.local.entity.BibleVersionEntity
import com.huaman.bible_hymns_choruses.data.local.entity.BookEntity
import com.huaman.bible_hymns_choruses.data.local.entity.BookmarkEntity
import com.huaman.bible_hymns_choruses.data.local.entity.NoteEntity
import com.huaman.bible_hymns_choruses.data.local.entity.SongEntity
import com.huaman.bible_hymns_choruses.data.local.entity.SongSummaryProjection
import com.huaman.bible_hymns_choruses.data.local.entity.VerseEntity
import com.huaman.bible_hymns_choruses.data.local.entity.VerseWithBookProjection
import com.huaman.bible_hymns_choruses.domain.model.BibleBook
import com.huaman.bible_hymns_choruses.domain.model.BibleVerse
import com.huaman.bible_hymns_choruses.domain.model.BibleVersion
import com.huaman.bible_hymns_choruses.domain.model.BookmarkItem
import com.huaman.bible_hymns_choruses.domain.model.BookmarkType
import com.huaman.bible_hymns_choruses.domain.model.NoteItem
import com.huaman.bible_hymns_choruses.domain.model.SongDetail
import com.huaman.bible_hymns_choruses.domain.model.SongStanzaBlock
import com.huaman.bible_hymns_choruses.domain.model.SongSummary
import com.huaman.bible_hymns_choruses.domain.model.SongType
import com.huaman.bible_hymns_choruses.domain.model.Testament
import com.huaman.bible_hymns_choruses.domain.model.VerseSearchMatch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun BibleVersionEntity.toDomain(): BibleVersion = BibleVersion(
    id = id,
    name = name,
    fullTitle = fullTitle,
    copyright = copyright,
    sortOrder = sortOrder
)

fun BookEntity.toDomain(): BibleBook = BibleBook(
    id = id,
    testament = if (testament.equals("AT", ignoreCase = true)) Testament.AT else Testament.NT,
    name = name,
    abbreviation = abbreviation,
    chapterCount = chapterCount
)

fun VerseEntity.toDomain(): BibleVerse = BibleVerse(
    id = id,
    versionId = versionId,
    bookId = bookId,
    chapter = chapter,
    verse = verse,
    verseEnd = verseEnd,
    verseLabel = verseLabel,
    heading = heading,
    label = label,
    text = text
)

fun VerseWithBookProjection.toDomain(): VerseSearchMatch = VerseSearchMatch(
    id = id,
    versionId = versionId,
    bookId = bookId,
    bookName = bookName,
    bookAbbr = bookAbbr,
    chapter = chapter,
    verse = verse,
    verseEnd = verseEnd,
    verseLabel = verseLabel,
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

fun NoteEntity.toDomain(): NoteItem = NoteItem(
    id = id,
    title = title,
    content = content,
    imagePaths = runCatching { Json.decodeFromString<List<String>>(imagePathsJson) }.getOrDefault(emptyList()),
    audioPaths = runCatching { Json.decodeFromString<List<String>>(audioPathsJson) }.getOrDefault(emptyList()),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun NoteItem.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    imagePathsJson = Json.encodeToString(imagePaths),
    audioPathsJson = Json.encodeToString(audioPaths),
    createdAt = createdAt,
    updatedAt = updatedAt
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
