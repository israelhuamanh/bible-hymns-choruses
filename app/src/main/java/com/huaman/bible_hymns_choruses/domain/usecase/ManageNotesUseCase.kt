package com.huaman.bible_hymns_choruses.domain.usecase

import com.huaman.bible_hymns_choruses.data.local.entity.NoteEntity
import com.huaman.bible_hymns_choruses.data.mapper.toDomain
import com.huaman.bible_hymns_choruses.data.repository.NoteRepository
import com.huaman.bible_hymns_choruses.domain.model.NoteItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ManageNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    fun observeAll(): Flow<List<NoteItem>> =
        noteRepository.getAllNotes().map { list -> list.map { it.toDomain() } }

    suspend fun save(
        id: Int = 0,
        title: String,
        content: String,
        imagePaths: List<String>,
        audioPaths: List<String>,
        createdAt: Long? = null
    ): Long {
        val now = System.currentTimeMillis()
        val entity = NoteEntity(
            id = id,
            title = title.trim(),
            content = content.trim(),
            imagePathsJson = Json.encodeToString(imagePaths),
            audioPathsJson = Json.encodeToString(audioPaths),
            createdAt = createdAt ?: now,
            updatedAt = now
        )
        return noteRepository.saveNote(entity)
    }

    suspend fun restore(note: NoteItem): Long {
        val entity = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            imagePathsJson = Json.encodeToString(note.imagePaths),
            audioPathsJson = Json.encodeToString(note.audioPaths),
            createdAt = note.createdAt,
            updatedAt = note.updatedAt
        )
        return noteRepository.saveNote(entity)
    }

    suspend fun removeById(id: Int) = noteRepository.deleteNoteById(id)
}
